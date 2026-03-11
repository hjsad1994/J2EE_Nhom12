package nhom12.example.nhom12.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
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
}
