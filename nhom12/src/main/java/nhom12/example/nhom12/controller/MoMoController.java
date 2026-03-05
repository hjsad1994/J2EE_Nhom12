package nhom12.example.nhom12.controller;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nhom12.example.nhom12.dto.response.ApiResponse;
import nhom12.example.nhom12.exception.ResourceNotFoundException;
import nhom12.example.nhom12.model.Order;
import nhom12.example.nhom12.model.User;
import nhom12.example.nhom12.repository.OrderRepository;
import nhom12.example.nhom12.repository.UserRepository;
import nhom12.example.nhom12.service.MoMoService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class MoMoController {

  // Frontend base URL for redirects
  private static final String FRONTEND_URL = "http://localhost:5173";

  private final MoMoService moMoService;
  private final OrderRepository orderRepository;
  private final UserRepository userRepository;

  /**
   * Initiate MoMo payment for an existing order.
   * Returns the MoMo payment URL.
   * Requires authentication.
   */
  @PostMapping("/api/momo/create")
  public ResponseEntity<ApiResponse<Map<String, String>>> createPayment(
      @RequestParam String orderId,
      @AuthenticationPrincipal UserDetails principal) {

    User user = userRepository.findByUsername(principal.getUsername())
        .orElseThrow(() -> new ResourceNotFoundException("User", "username", principal.getUsername()));

    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

    // Security: ensure the order belongs to the requesting user
    if (!order.getUserId().equals(user.getId())) {
      return ResponseEntity.status(403)
          .body(ApiResponse.<Map<String, String>>builder()
              .status(403).message("Access denied: order does not belong to you").build());
    }

    // Prevent duplicate payments
    if ("PAID".equals(order.getPaymentStatus())) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.<Map<String, String>>builder()
              .status(400).message("Order has already been paid").build());
    }

    long amount = Math.round(order.getTotal());
    String orderInfo = "Thanh toan don hang #" + orderId;

    String payUrl = moMoService.initiatePayment(orderId, amount, orderInfo);

    Map<String, String> result = new HashMap<>();
    result.put("payUrl", payUrl);
    return ResponseEntity.ok(ApiResponse.success(result, "Payment URL created successfully"));
  }

  /**
   * MoMo redirects the user browser here after payment (success or failure).
   * Verifies signature and redirects to frontend result page.
   */
  @GetMapping("/momo/return")
  public void momoReturn(
      @RequestParam Map<String, String> params,
      HttpServletResponse response) throws IOException {

    // Use mutable copy since @RequestParam maps are unmodifiable
    Map<String, String> mutableParams = new HashMap<>(params);
    String orderId = mutableParams.get("orderId");
    String resultCode = mutableParams.get("resultCode");
    String signature = mutableParams.remove("signature");

    log.info("[MoMo] Return callback: orderId={}, resultCode={}", orderId, resultCode);

    boolean valid = moMoService.verifySignature(mutableParams, signature);
    if (!valid) {
      log.error("[MoMo] Invalid signature on return callback for orderId={}", orderId);
      response.sendRedirect(FRONTEND_URL + "/checkout/result?success=false&error=invalid_signature");
      return;
    }

    // Only process if not already handled by IPN
    try {
      Order order = orderRepository.findById(orderId).orElse(null);
      if (order != null && !"PAID".equals(order.getPaymentStatus())
          && !"FAILED".equals(order.getPaymentStatus())) {
        moMoService.processPaymentResult(mutableParams);
      }
    } catch (Exception e) {
      log.error("[MoMo] Error processing return for orderId={}: {}", orderId, e.getMessage());
    }

    boolean success = "0".equals(resultCode);
    response.sendRedirect(FRONTEND_URL + "/checkout/result?success=" + success + "&orderId=" + orderId);
  }

  /**
   * MoMo sends async IPN (Instant Payment Notification) here.
   * Must return HTTP 200 with resultCode=0 to acknowledge receipt.
   */
  @PostMapping("/momo/ipn")
  public ResponseEntity<Map<String, Object>> momoIpn(
      @RequestBody Map<String, Object> rawParams) {

    // Convert all values to String for uniform handling
    Map<String, String> params = new HashMap<>();
    rawParams.forEach((k, v) -> params.put(k, v != null ? v.toString() : ""));

    String orderId = params.get("orderId");
    String signature = params.remove("signature");

    log.info("[MoMo] IPN received: orderId={}, resultCode={}", orderId, params.get("resultCode"));

    Map<String, Object> ackResponse = new HashMap<>();

    boolean valid = moMoService.verifySignature(params, signature);
    if (!valid) {
      log.error("[MoMo] Invalid IPN signature for orderId={}", orderId);
      ackResponse.put("resultCode", -1);
      ackResponse.put("message", "Invalid signature");
      return ResponseEntity.ok(ackResponse);
    }

    try {
      moMoService.processPaymentResult(params);
      ackResponse.put("resultCode", 0);
      ackResponse.put("message", "Success");
    } catch (Exception e) {
      log.error("[MoMo] Error processing IPN for orderId={}: {}", orderId, e.getMessage());
      ackResponse.put("resultCode", -1);
      ackResponse.put("message", e.getMessage());
    }

    return ResponseEntity.ok(ackResponse);
  }
}
