package nhom12.example.nhom12.controller;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import nhom12.example.nhom12.dto.request.CreateUserRequest;
import nhom12.example.nhom12.dto.request.ForgotPasswordRequest;
import nhom12.example.nhom12.dto.request.LoginRequest;
import nhom12.example.nhom12.dto.request.ResetPasswordRequest;
import nhom12.example.nhom12.dto.response.ApiResponse;
import nhom12.example.nhom12.dto.response.AuthResponse;
import nhom12.example.nhom12.exception.BadRequestException;
import nhom12.example.nhom12.model.PasswordResetToken;
import nhom12.example.nhom12.model.User;
import nhom12.example.nhom12.repository.PasswordResetTokenRepository;
import nhom12.example.nhom12.repository.UserRepository;
import nhom12.example.nhom12.service.AuthService;
import nhom12.example.nhom12.service.EmailService;
import nhom12.example.nhom12.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;
  private final UserRepository userRepository;
  private final PasswordResetTokenRepository passwordResetTokenRepository;
  private final EmailService emailService;
  private final UserService userService;

  @PostMapping("/register")
  public ResponseEntity<ApiResponse<AuthResponse>> register(
      @Valid @RequestBody CreateUserRequest request) {
    AuthResponse response = authService.register(request);
    return new ResponseEntity<>(
        ApiResponse.created(response, "User registered successfully"), HttpStatus.CREATED);
  }

  @PostMapping("/login")
  public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
    AuthResponse response = authService.login(request);
    return ResponseEntity.ok(ApiResponse.success(response, "Login successful"));
  }

  @PostMapping("/forgot-password")
  public ResponseEntity<ApiResponse<Void>> forgotPassword(
      @Valid @RequestBody ForgotPasswordRequest request) {
    User user =
        userRepository
            .findByEmail(request.getEmail())
            .orElse(null);

    // Always return success to prevent email enumeration
    if (user == null) {
      return ResponseEntity.ok(
          ApiResponse.success(
              null,
              "Nếu email tồn tại trong hệ thống, bạn sẽ nhận được link đặt lại mật khẩu"));
    }

    // Delete any existing tokens for this user
    passwordResetTokenRepository.deleteByUserId(user.getId());

    // Generate new token
    String token = UUID.randomUUID().toString();
    PasswordResetToken resetToken =
        PasswordResetToken.builder()
            .userId(user.getId())
            .token(token)
            .expiresAt(LocalDateTime.now().plusMinutes(15))
            .build();
    passwordResetTokenRepository.save(resetToken);

    // Send email
    emailService.sendPasswordResetEmail(user.getEmail(), token);

    return ResponseEntity.ok(
        ApiResponse.success(
            null,
            "Nếu email tồn tại trong hệ thống, bạn sẽ nhận được link đặt lại mật khẩu"));
  }

  @PostMapping("/reset-password")
  public ResponseEntity<ApiResponse<Void>> resetPassword(
      @Valid @RequestBody ResetPasswordRequest request) {
    PasswordResetToken resetToken =
        passwordResetTokenRepository
            .findByToken(request.getToken())
            .orElseThrow(() -> new BadRequestException("Token không hợp lệ hoặc đã hết hạn"));

    if (resetToken.isExpired()) {
      passwordResetTokenRepository.deleteById(resetToken.getId());
      throw new BadRequestException("Token không hợp lệ hoặc đã hết hạn");
    }

    // Reset the password
    userService.resetPassword(resetToken.getUserId(), request.getNewPassword());

    // Clean up the token
    passwordResetTokenRepository.deleteByUserId(resetToken.getUserId());

    return ResponseEntity.ok(ApiResponse.success(null, "Đặt lại mật khẩu thành công"));
  }
}
