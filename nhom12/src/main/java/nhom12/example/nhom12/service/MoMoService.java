package nhom12.example.nhom12.service;

import java.util.Map;

public interface MoMoService {

  /**
   * Initiate a MoMo payment for an existing order.
   *
   * @param orderId MongoDB document ID used as MoMo orderId
   * @param amount order total in VND (long)
   * @param orderInfo human-readable description
   * @return MoMo payment URL to redirect the user
   */
  String initiatePayment(String orderId, long amount, String orderInfo);

  /**
   * Verify HMAC-SHA256 signature from MoMo IPN / return callback.
   *
   * @param params all parameters received from MoMo (excluding signature)
   * @param signature the signature value to verify against
   * @return true if signature matches
   */
  boolean verifySignature(Map<String, String> params, String signature);

  /**
   * Process the payment result from IPN or return callback. Updates order paymentStatus and status
   * accordingly.
   *
   * @param params all parameters from MoMo callback
   */
  void processPaymentResult(Map<String, String> params);
}
