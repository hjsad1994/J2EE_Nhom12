package nhom12.example.nhom12.service.impl;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nhom12.example.nhom12.config.MoMoConfig;
import nhom12.example.nhom12.model.Order;
import nhom12.example.nhom12.model.OrderItem;
import nhom12.example.nhom12.model.Product;
import nhom12.example.nhom12.model.ProductVariant;
import nhom12.example.nhom12.model.enums.OrderStatus;
import nhom12.example.nhom12.repository.ProductRepository;
import nhom12.example.nhom12.service.EmailService;
import nhom12.example.nhom12.service.MoMoService;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class MoMoServiceImpl implements MoMoService {

  private static final String REQUEST_TYPE = "payWithMethod";

  private final MoMoConfig moMoConfig;
  private final ProductRepository productRepository;
  private final EmailService emailService;
  private final MongoTemplate mongoTemplate;
  private final RestTemplate restTemplate;

  @Override
  public String initiatePayment(String orderId, long amount, String orderInfo) {
    String requestId = orderId + "_" + System.currentTimeMillis();
    String extraData = "";
    String orderGroupId = "";

    // Build raw signature string (params in alphabetical order)
    String rawHash =
        "accessKey="
            + moMoConfig.getAccessKey()
            + "&amount="
            + amount
            + "&extraData="
            + extraData
            + "&ipnUrl="
            + moMoConfig.getIpnUrl()
            + "&orderId="
            + orderId
            + "&orderInfo="
            + orderInfo
            + "&partnerCode="
            + moMoConfig.getPartnerCode()
            + "&redirectUrl="
            + moMoConfig.getRedirectUrl()
            + "&requestId="
            + requestId
            + "&requestType="
            + REQUEST_TYPE;

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

    ResponseEntity<Map> response =
        restTemplate.postForEntity(moMoConfig.getApiUrl(), request, Map.class);

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
    String rawHash =
        "accessKey="
            + moMoConfig.getAccessKey()
            + "&amount="
            + params.get("amount")
            + "&extraData="
            + params.getOrDefault("extraData", "")
            + "&message="
            + params.getOrDefault("message", "")
            + "&orderId="
            + params.get("orderId")
            + "&orderInfo="
            + params.getOrDefault("orderInfo", "")
            + "&orderType="
            + params.getOrDefault("orderType", "")
            + "&partnerCode="
            + params.get("partnerCode")
            + "&payType="
            + params.getOrDefault("payType", "")
            + "&requestId="
            + params.get("requestId")
            + "&responseTime="
            + params.getOrDefault("responseTime", "")
            + "&resultCode="
            + params.get("resultCode")
            + "&transId="
            + params.getOrDefault("transId", "");

    String computed = hmacSHA256(rawHash, moMoConfig.getSecretKey());
    boolean valid = computed.equals(signature);
    if (!valid) {
      log.warn("[MoMo] Signature verification failed for orderId={}", params.get("orderId"));
    }
    return valid;
  }

  /**
   * Processes the MoMo payment callback atomically:
   *
   * <ul>
   *   <li>Success: marks order PAID + CONFIRMED in one transaction.
   *   <li>Failure: marks order FAILED + CANCELLED AND restores stock — both in one transaction.
   * </ul>
   *
   * Without @Transactional, a crash between updating the order and restoring stock would leave the
   * system in an inconsistent state (cancelled order but stock not restored).
   */
  @Override
  @Transactional
  public void processPaymentResult(Map<String, String> params) {
    String orderId = params.get("orderId");
    String resultCodeStr = params.get("resultCode");
    String transId = params.getOrDefault("transId", "");
    String message = params.getOrDefault("message", "");

    log.info(
        "[MoMo] Processing payment result: orderId={}, resultCode={}, transId={}",
        orderId,
        resultCodeStr,
        transId);

    int resultCode = Integer.parseInt(resultCodeStr);
    Query pendingOrderQuery =
        Query.query(Criteria.where("_id").is(orderId).and("paymentStatus").is("PENDING"));
    Update update = new Update().set("momoTransId", transId);

    if (resultCode == 0) {
      update.set("paymentStatus", "PAID").set("status", OrderStatus.CONFIRMED);
    } else {
      update
          .set("paymentStatus", "FAILED")
          .set("status", OrderStatus.CANCELLED)
          .set("cancelledBy", "SYSTEM")
          .set(
              "cancelReason",
              message == null || message.isBlank() ? "Thanh toán MoMo thất bại" : message);
    }

    Order order =
        mongoTemplate.findAndModify(
            pendingOrderQuery,
            update,
            FindAndModifyOptions.options().returnNew(false),
            Order.class);

    if (order == null) {
      log.info(
          "[MoMo] Skip callback for orderId={} because it was already processed or no longer"
              + " pending",
          orderId);
      return;
    }

    if (resultCode == 0) {
      emailService.sendOrderConfirmationEmail(order);
      log.info("[MoMo] Payment successful for orderId={}, transId={}", orderId, transId);
    } else {
      // Restore stock atomically with order cancellation
      restoreStock(order);
      log.warn(
          "[MoMo] Payment failed for orderId={}, resultCode={}, message={}. Stock restored.",
          orderId,
          resultCode,
          message);
    }
  }

  private void restoreStock(Order order) {
    for (OrderItem item : order.getItems()) {
      productRepository
          .findById(item.getProductId())
          .ifPresent(
              product -> {
                ProductVariant variant = findVariant(product, item.getColor(), item.getStorage());
                if (variant != null) {
                  variant.setStock(variant.getStock() + item.getQuantity());
                  syncSummaryFieldsFromVariants(product);
                } else {
                  product.setStock(product.getStock() + item.getQuantity());
                }
                productRepository.save(product);
              });
    }
  }

  private ProductVariant findVariant(Product product, String color, String storage) {
    String normalizedColor = normalizeOption(color);
    String normalizedStorage = normalizeOption(storage);

    if (normalizedColor.isBlank() && normalizedStorage.isBlank()) {
      return null;
    }

    if (product.getVariants() == null) {
      return null;
    }

    return product.getVariants().stream()
        .filter(
            variant ->
                normalizeOption(variant.getColor()).equals(normalizedColor)
                    && normalizeOption(variant.getStorage()).equals(normalizedStorage))
        .findFirst()
        .orElse(null);
  }

  private void syncSummaryFieldsFromVariants(Product product) {
    if (product.getVariants() == null || product.getVariants().isEmpty()) {
      return;
    }

    ProductVariant primaryVariant = product.getVariants().get(0);
    product.setPrice(primaryVariant.getPrice());
    product.setStock(product.getVariants().stream().mapToInt(ProductVariant::getStock).sum());
  }

  private String normalizeOption(String value) {
    return value == null ? "" : value.trim();
  }

  private String hmacSHA256(String data, String key) {
    try {
      Mac mac = Mac.getInstance("HmacSHA256");
      SecretKeySpec secretKey =
          new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
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
