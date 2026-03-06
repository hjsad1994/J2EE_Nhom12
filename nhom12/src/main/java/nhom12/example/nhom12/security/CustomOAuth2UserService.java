package nhom12.example.nhom12.security;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import nhom12.example.nhom12.model.User;
import nhom12.example.nhom12.model.enums.Role;
import nhom12.example.nhom12.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Override
  public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
    OAuth2User oauth2User = super.loadUser(userRequest);

    String googleId = oauth2User.getAttribute("sub");
    String email = oauth2User.getAttribute("email");
    String name = oauth2User.getAttribute("name");

    findOrCreateUser(googleId, email, name);

    return oauth2User;
  }

  private User findOrCreateUser(String googleId, String email, String name) {
    // 1. Check by googleId
    return userRepository
        .findByGoogleId(googleId)
        .orElseGet(
            () ->
                userRepository
                    .findByEmail(email)
                    .map(
                        existing -> {
                          // Link Google account to existing email account
                          existing.setGoogleId(googleId);
                          return userRepository.save(existing);
                        })
                    .orElseGet(() -> createGoogleUser(googleId, email, name)));
  }

  private User createGoogleUser(String googleId, String email, String name) {
    String username = generateUsername(email, name);
    return userRepository.save(
        User.builder()
            .googleId(googleId)
            .email(email)
            .username(username)
            .password(passwordEncoder.encode(UUID.randomUUID().toString()))
            .role(Role.USER)
            .build());
  }

  private String generateUsername(String email, String name) {
    // Try sanitized name first
    if (name != null && !name.isBlank()) {
      String sanitized = name.trim().toLowerCase().replaceAll("[^a-z0-9_]", "_");
      if (!userRepository.existsByUsername(sanitized)) {
        return sanitized;
      }
    }
    // Try email local part
    String localPart = email.split("@")[0].replaceAll("[^a-z0-9_]", "_").toLowerCase();
    if (!userRepository.existsByUsername(localPart)) {
      return localPart;
    }
    // Append short random suffix
    return localPart + "_" + UUID.randomUUID().toString().substring(0, 5);
  }
}
