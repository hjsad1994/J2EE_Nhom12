package nhom12.example.nhom12.service.impl;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nhom12.example.nhom12.config.MoMoConfig;
import nhom12.example.nhom12.exception.ResourceNotFoundException;
import nhom12.example.nhom12.model.Order;
import nhom12.example.nhom12.model.enums.OrderStatus;
import nhom12.example.nhom12.repository.OrderRepository;
import nhom12.example.nhom12.service.MoMoService;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class MoMoServiceImpl implements MoMoService {

  private static final String REQUEST_TYPE = "payWithMethod";

  private final MoMoConfig moMoConfig;
  private final OrderRepository orderRepository;
  private final RestTemplate restTemplate;

  @Override
  public String initiatePayment(String orderId, long amount, String orderInfo) {
    String requestId = orderId + "_" + System.currentTimeMillis();
    String extraData = "";
    String orderGroupId = "";

    // Build raw signature string (params in alphabetical order)
    String rawHash = "accessKey=" + moMoConfig.getAccessKey()
        + "&amount=" + amount
        + "&extraData=" + extraData
        + "&ipnUrl=" + moMoConfig.getIpnUrl()
        + "&orderId=" + orderId
        + "&orderInfo=" + orderInfo
        + "&partnerCode=" + moMoConfig.getPartnerCode()
        + "&redirectUrl=" + moMoConfig.getRedirectUrl()
        + "&requestId=" + requestId
        + "&requestType=" + REQUEST_TYPE;

    String signature = hmacSHA256(rawHash, moMoConfig.getSecretKey());

    // Build JSON request body
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("partnerCode", moMoConfig.getPartnerCode());
    body.put("requestType", REQUEST_TYPE);
    body.put("ipnUrl", moMoConfig.getIpnUrl());
    body.put("redirectUrl", moMoConfig.getRedirectUrl());
    body.put("orderId", orderId);
    body.put("amount", amount);
    body.put("lang", "vi");
    body.put("autoCapture", true);
    body.put("orderInfo", orderInfo);
    body.put("requestId", requestId);
    body.put("extraData", extraData);
    body.put("orderGroupId", orderGroupId);
    body.put("signature", signature);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

    log.info("[MoMo] Creating payment request for orderId={}, amount={}", orderId, amount);

    ResponseEntity<Map> response = restTemplate.postForEntity(
        moMoConfig.getApiUrl(), request, Map.class);

    Map<?, ?> responseBody = response.getBody();
    if (responseBody == null) {
      throw new RuntimeException("MoMo API returned empty response");
    }

    int resultCode = ((Number) responseBody.get("resultCode")).intValue();
    if (resultCode != 0) {
      String message = (String) responseBody.get("message");
      log.error("[MoMo] Payment creation failed: resultCode={}, message={}", resultCode, message);
      throw new RuntimeException("MoMo payment creation failed: " + message);
    }

    String payUrl = (String) responseBody.get("payUrl");
    log.info("[MoMo] Payment URL created successfully for orderId={}", orderId);
    return payUrl;
  }

  @Override
  public boolean verifySignature(Map<String, String> params, String signature) {
    // IPN/Return signature raw string (params in alphabetical order)
    String rawHash = "accessKey=" + moMoConfig.getAccessKey()
        + "&amount=" + params.get("amount")
        + "&extraData=" + params.getOrDefault("extraData", "")
        + "&message=" + params.getOrDefault("message", "")
        + "&orderId=" + params.get("orderId")
        + "&orderInfo=" + params.getOrDefault("orderInfo", "")
        + "&orderType=" + params.getOrDefault("orderType", "")
        + "&partnerCode=" + params.get("partnerCode")
        + "&payType=" + params.getOrDefault("payType", "")
        + "&requestId=" + params.get("requestId")
        + "&responseTime=" + params.getOrDefault("responseTime", "")
        + "&resultCode=" + params.get("resultCode")
        + "&transId=" + params.getOrDefault("transId", "");

    String computed = hmacSHA256(rawHash, moMoConfig.getSecretKey());
    boolean valid = computed.equals(signature);
    if (!valid) {
      log.warn("[MoMo] Signature verification failed for orderId={}", params.get("orderId"));
    }
    return valid;
  }

  @Override
  public void processPaymentResult(Map<String, String> params) {
    String orderId = params.get("orderId");
    String resultCodeStr = params.get("resultCode");
    String transId = params.getOrDefault("transId", "");
    String message = params.getOrDefault("message", "");

    log.info("[MoMo] Processing payment result: orderId={}, resultCode={}, transId={}",
        orderId, resultCodeStr, transId);

    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

    int resultCode = Integer.parseInt(resultCodeStr);
    if (resultCode == 0) {
      order.setPaymentStatus("PAID");
      order.setStatus(OrderStatus.CONFIRMED);
      order.setMomoTransId(transId);
      log.info("[MoMo] Payment successful for orderId={}, transId={}", orderId, transId);
    } else {
      order.setPaymentStatus("FAILED");
      order.setStatus(OrderStatus.CANCELLED);
      log.warn("[MoMo] Payment failed for orderId={}, resultCode={}, message={}",
          orderId, resultCode, message);
    }

    orderRepository.save(order);
  }

  private String hmacSHA256(String data, String key) {
    try {
      Mac mac = Mac.getInstance("HmacSHA256");
      SecretKeySpec secretKey = new SecretKeySpec(
          key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
      mac.init(secretKey);
      byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
      StringBuilder hex = new StringBuilder();
      for (byte b : hash) {
        String h = Integer.toHexString(0xff & b);
        if (h.length() == 1) hex.append('0');
        hex.append(h);
      }
      return hex.toString();
    } catch (Exception e) {
      throw new RuntimeException("Failed to compute HMAC SHA256", e);
    }
  }
}
