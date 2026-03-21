package nhom12.example.nhom12.service;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nhom12.example.nhom12.model.Order;
import nhom12.example.nhom12.model.OrderItem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

  private static final Locale VIETNAM_LOCALE = Locale.forLanguageTag("vi-VN");

  private final JavaMailSender mailSender;
  private final RestTemplate restTemplate;

  @Value("${app.frontend-url}")
  private String frontendUrl;

  @Value("${app.email.provider:smtp}")
  private String emailProvider;

  @Value("${app.email.from:${MAIL_USERNAME:no-reply@localhost}}")
  private String fromEmail;

  @Value("${resend.api-key:}")
  private String resendApiKey;

  @Value("${resend.api-url:https://api.resend.com/emails}")
  private String resendApiUrl;

  @Async
  public void sendPasswordResetEmail(String to, String token) {
    try {
      String resetLink = frontendUrl + "/reset-password?token=" + token;
      sendEmail(
          to,
          "VeritaShop | Yeu cau dat lai mat khau",
          "Xin chao,\n\n"
              + "Ban da yeu cau dat lai mat khau.\n\n"
              + "Nhan vao link sau de dat lai: "
              + resetLink
              + "\n\n"
              + "Link co hieu luc trong 15 phut.\n\n"
              + "Neu ban khong yeu cau, hay bo qua email nay.\n\n"
              + "VeritaShop");
      log.info("[Email] Password reset email sent to {}", to);
    } catch (Exception e) {
      log.error("[Email] Failed to send password reset email to {}: {}", to, e.getMessage());
    }
  }

  @Async
  public void sendPasswordChangedEmail(String to, String username) {
    try {
      sendEmail(
          to,
          "VeritaShop | Mat khau cua ban da duoc thay doi",
          "Xin chao "
              + username
              + ",\n\n"
              + "Mat khau tai khoan cua ban vua duoc thay doi thanh cong.\n\n"
              + "Neu day khong phai la ban, hay dat lai mat khau ngay va lien he quan tri vien.\n\n"
              + "VeritaShop");
      log.info("[Email] Password changed email sent to {}", to);
    } catch (Exception e) {
      log.error("[Email] Failed to send password changed email to {}: {}", to, e.getMessage());
    }
  }

  @Async
  public void sendOrderConfirmationEmail(Order order) {
    try {
      sendEmail(
          order.getEmail(),
          "VeritaShop | Xac nhan don hang #" + shortOrderCode(order.getOrderCode()),
          buildOrderConfirmationBody(order));
      log.info(
          "[Email] Order confirmation sent to {} for orderCode={}",
          order.getEmail(),
          order.getOrderCode());
    } catch (Exception e) {
      log.error(
          "[Email] Failed to send order confirmation to {}: {}",
          order.getEmail(),
          e.getMessage());
    }
  }

  @Async
  public void sendOrderConfirmationEmail(
      String to, String customerName, String orderCode, double total) {
    try {
      sendEmail(
          to,
          "VeritaShop | Xac nhan don hang #" + shortOrderCode(orderCode),
          "Xin chao "
              + customerName
              + ",\n\n"
              + "Don hang cua ban da duoc dat thanh cong.\n\n"
              + "Ma don hang: "
              + shortOrderCode(orderCode)
              + "\n"
              + "Tong cong: "
              + formatCurrency(total)
              + "\n\n"
              + "VeritaShop");
      log.info("[Email] Order confirmation sent to {} for orderCode={}", to, orderCode);
    } catch (Exception e) {
      log.error("[Email] Failed to send order confirmation to {}: {}", to, e.getMessage());
    }
  }

  private String buildOrderConfirmationBody(Order order) {
    StringBuilder body =
        new StringBuilder()
            .append("Xin chao ")
            .append(order.getCustomerName())
            .append(",\n\n")
            .append("Don hang cua ban da duoc dat thanh cong.\n\n")
            .append("Ma don hang: ")
            .append(shortOrderCode(order.getOrderCode()))
            .append("\n")
            .append("Phuong thuc thanh toan: ")
            .append(order.getPaymentMethod())
            .append("\n")
            .append("Trang thai thanh toan: ")
            .append(order.getPaymentStatus())
            .append("\n\n")
            .append("San pham da dat:\n");

    for (OrderItem item : order.getItems()) {
      body.append("- ").append(item.getProductName());
      if ((item.getColor() != null && !item.getColor().isBlank())
          || (item.getStorage() != null && !item.getStorage().isBlank())) {
        body.append(" (")
            .append(
                String.join(
                    " · ",
                    java.util.stream.Stream.of(item.getColor(), item.getStorage())
                        .filter(value -> value != null && !value.isBlank())
                        .toList()))
            .append(")");
      }
      body.append(": ")
          .append(item.getQuantity())
          .append(" x ")
          .append(formatCurrency(item.getPrice()))
          .append(" = ")
          .append(formatCurrency(item.getPrice() * item.getQuantity()))
          .append("\n");
    }

    body.append("\n").append("Tam tinh: ").append(formatCurrency(order.getSubtotal())).append("\n");

    if (order.getProductDiscount() > 0) {
      body.append("Giam san pham");
      if (order.getProductVoucher() != null) {
        body.append(" (").append(order.getProductVoucher().getCode()).append(")");
      }
      body.append(": -").append(formatCurrency(order.getProductDiscount())).append("\n");
    }

    body.append("Phi van chuyen: ")
        .append(formatCurrency(order.getOriginalShippingFee()))
        .append("\n");

    if (order.getShippingDiscount() > 0) {
      body.append("Giam phi van chuyen");
      if (order.getShippingVoucher() != null) {
        body.append(" (").append(order.getShippingVoucher().getCode()).append(")");
      }
      body.append(": -").append(formatCurrency(order.getShippingDiscount())).append("\n");
    }

    body.append("Tong cong: ")
        .append(formatCurrency(order.getTotal()))
        .append("\n\n")
        .append("Thong tin nhan hang:\n")
        .append("- Nguoi nhan: ")
        .append(order.getCustomerName())
        .append("\n")
        .append("- Dien thoai: ")
        .append(order.getPhone())
        .append("\n")
        .append("- Dia chi: ")
        .append(order.getAddress())
        .append(", ")
        .append(order.getWard())
        .append(", ")
        .append(order.getDistrict())
        .append(", ")
        .append(order.getCity())
        .append("\n");

    if (order.getNote() != null && !order.getNote().isBlank()) {
      body.append("- Ghi chu: ").append(order.getNote()).append("\n");
    }

    body.append("\nChung toi se xu ly don hang cua ban trong thoi gian som nhat.\n\nVeritaShop");
    return body.toString();
  }

  private String shortOrderCode(String orderCode) {
    return orderCode.substring(0, Math.min(8, orderCode.length())).toUpperCase();
  }

  private String formatCurrency(double amount) {
    return NumberFormat.getNumberInstance(VIETNAM_LOCALE).format(Math.round(amount)) + " VND";
  }

  private void sendEmail(String to, String subject, String text) {
    if ("resend".equalsIgnoreCase(emailProvider)) {
      sendViaResend(to, subject, text);
      return;
    }

    sendViaSmtp(to, subject, text);
  }

  private void sendViaSmtp(String to, String subject, String text) {
    SimpleMailMessage message = new SimpleMailMessage();
    message.setFrom(fromEmail);
    message.setTo(to);
    message.setSubject(subject);
    message.setText(text);
    mailSender.send(message);
  }

  private void sendViaResend(String to, String subject, String text) {
    if (resendApiKey == null || resendApiKey.isBlank()) {
      throw new IllegalStateException("RESEND_API_KEY is missing");
    }

    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(resendApiKey);
    headers.setContentType(MediaType.APPLICATION_JSON);

    Map<String, Object> payload =
        Map.of(
            "from", fromEmail,
            "to", List.of(to),
            "subject", subject,
            "text", text);

    restTemplate.postForEntity(resendApiUrl, new HttpEntity<>(payload, headers), Map.class);
  }
}
