package nhom12.example.nhom12.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

  private final JavaMailSender mailSender;

  @Value("${app.frontend-url}")
  private String frontendUrl;

  public void sendPasswordResetEmail(String to, String token) {
    String resetLink = frontendUrl + "/reset-password?token=" + token;
    SimpleMailMessage message = new SimpleMailMessage();
    message.setTo(to);
    message.setSubject("Đặt lại mật khẩu - NEBULA Store");
    message.setText(
        "Xin chào,\n\n"
            + "Bạn đã yêu cầu đặt lại mật khẩu.\n\n"
            + "Nhấn vào link sau để đặt lại: "
            + resetLink
            + "\n\n"
            + "Link có hiệu lực trong 15 phút.\n\n"
            + "Nếu bạn không yêu cầu, hãy bỏ qua email này.\n\n"
            + "NEBULA Store");
    mailSender.send(message);
  }

  /**
   * Sends an order confirmation email asynchronously (@Async). The caller (OrderServiceImpl) does
   * NOT wait for this to complete — the HTTP response is returned immediately after the order is
   * saved, and this email is processed in the background by Spring's task executor.
   */
  @Async
  public void sendOrderConfirmationEmail(
      String to, String customerName, String orderCode, double total) {
    try {
      SimpleMailMessage message = new SimpleMailMessage();
      message.setTo(to);
      message.setSubject("Đặt hàng thành công - NEBULA Store");
      message.setText(
          "Xin chào "
              + customerName
              + ",\n\n"
              + "Đơn hàng của bạn đã được đặt thành công!\n\n"
              + "Mã đơn hàng: "
              + orderCode.substring(0, Math.min(8, orderCode.length())).toUpperCase()
              + "\n"
              + "Tổng tiền: "
              + String.format("%,.0f", total)
              + " VNĐ\n\n"
              + "Chúng tôi sẽ xử lý đơn hàng của bạn trong thời gian sớm nhất.\n\n"
              + "NEBULA Store");
      mailSender.send(message);
      log.info("[Email] Order confirmation sent to {} for orderCode={}", to, orderCode);
    } catch (Exception e) {
      // Email failure must NOT affect the order transaction — log and continue
      log.error("[Email] Failed to send order confirmation to {}: {}", to, e.getMessage());
    }
  }
}
