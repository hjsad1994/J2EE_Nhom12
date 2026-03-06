package nhom12.example.nhom12.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import nhom12.example.nhom12.model.User;
import nhom12.example.nhom12.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

  private final JwtUtil jwtUtil;
  private final CustomUserDetailsService userDetailsService;
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Value("${app.frontend-url}")
  private String frontendUrl;

  @Override
  public void onAuthenticationSuccess(
      HttpServletRequest request, HttpServletResponse response, Authentication authentication)
      throws IOException {

    OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
    String googleId = token.getPrincipal().getAttribute("sub");
    String email = token.getPrincipal().getAttribute("email");

    User user =
        userRepository
            .findByGoogleId(googleId)
            .or(() -> userRepository.findByEmail(email))
            .orElseGet(
                () -> {
                  // Fallback: user was not saved by CustomOAuth2UserService (e.g. write failed)
                  // Create a minimal user record now so login can proceed
                  String name = token.getPrincipal().getAttribute("name");
                  String base =
                      (name != null && !name.isBlank())
                          ? name.trim().toLowerCase().replaceAll("[^a-z0-9_]", "_")
                          : email.split("@")[0].toLowerCase().replaceAll("[^a-z0-9_]", "_");
                  String username =
                      userRepository.existsByUsername(base)
                          ? base + "_" + java.util.UUID.randomUUID().toString().substring(0, 5)
                          : base;
                  return userRepository.save(
                      User.builder()
                          .googleId(googleId)
                          .email(email)
                          .username(username)
                          .password(passwordEncoder.encode(java.util.UUID.randomUUID().toString()))
                          .role(nhom12.example.nhom12.model.enums.Role.USER)
                          .build());
                });

    UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
    String jwt = jwtUtil.generateToken(userDetails);

    String redirectUrl =
        frontendUrl
            + "/oauth2/callback"
            + "?token="
            + encode(jwt)
            + "&id="
            + encode(user.getId())
            + "&username="
            + encode(user.getUsername())
            + "&email="
            + encode(user.getEmail())
            + "&role="
            + encode(user.getRole().name());

    response.sendRedirect(redirectUrl);
  }

  private String encode(String value) {
    return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
  }
}
