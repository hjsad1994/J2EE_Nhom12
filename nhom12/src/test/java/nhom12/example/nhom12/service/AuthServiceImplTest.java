package nhom12.example.nhom12.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import nhom12.example.nhom12.dto.request.CreateUserRequest;
import nhom12.example.nhom12.dto.request.LoginRequest;
import nhom12.example.nhom12.dto.response.AuthResponse;
import nhom12.example.nhom12.exception.BadRequestException;
import nhom12.example.nhom12.exception.DuplicateResourceException;
import nhom12.example.nhom12.mapper.UserMapper;
import nhom12.example.nhom12.model.User;
import nhom12.example.nhom12.model.enums.Role;
import nhom12.example.nhom12.repository.UserRepository;
import nhom12.example.nhom12.security.CustomUserDetailsService;
import nhom12.example.nhom12.security.JwtUtil;
import nhom12.example.nhom12.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthServiceImpl - Unit Tests")
class AuthServiceImplTest {

  @Mock private UserRepository userRepository;
  @Mock private UserMapper userMapper;
  @Mock private PasswordEncoder passwordEncoder;
  @Mock private JwtUtil jwtUtil;
  @Mock private CustomUserDetailsService userDetailsService;
  @Mock private UserDetails userDetails;

  @InjectMocks private AuthServiceImpl authService;

  private CreateUserRequest registerRequest;
  private LoginRequest loginRequest;
  private User savedUser;

  @BeforeEach
  void setUp() {
    registerRequest = new CreateUserRequest("testuser", "test@example.com", "password123");

    loginRequest = new LoginRequest();
    loginRequest.setUsername("testuser");
    loginRequest.setPassword("password123");

    savedUser = User.builder()
        .username("testuser")
        .email("test@example.com")
        .role(Role.USER)
        .hasPassword(true)
        .banned(false)
        .build();
    // Simulate MongoDB id assignment
    savedUser.setId("user123");
  }

  // ───────────────────────────────────────────────────
  // REGISTER
  // ───────────────────────────────────────────────────
  @Nested
  @DisplayName("register()")
  class Register {

    @Test
    @DisplayName("Đăng ký thành công - trả về AuthResponse với JWT token")
    void register_success_returnsAuthResponseWithToken() {
      when(userRepository.existsByUsername("testuser")).thenReturn(false);
      when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
      when(userMapper.toEntity(registerRequest)).thenReturn(savedUser);
      when(passwordEncoder.encode("password123")).thenReturn("hashed_password");
      when(userRepository.save(any(User.class))).thenReturn(savedUser);
      when(userDetailsService.loadUserByUsername("testuser")).thenReturn(userDetails);
      when(jwtUtil.generateToken(userDetails)).thenReturn("jwt_token_123");

      AuthResponse result = authService.register(registerRequest);

      assertThat(result).isNotNull();
      assertThat(result.getToken()).isEqualTo("jwt_token_123");
      assertThat(result.getUsername()).isEqualTo("testuser");
      assertThat(result.getEmail()).isEqualTo("test@example.com");
      assertThat(result.getRole()).isEqualTo(Role.USER);
      verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Đăng ký thất bại - username đã tồn tại")
    void register_duplicateUsername_throwsDuplicateResourceException() {
      when(userRepository.existsByUsername("testuser")).thenReturn(true);

      assertThatThrownBy(() -> authService.register(registerRequest))
          .isInstanceOf(DuplicateResourceException.class)
          .hasMessageContaining("testuser");

      verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Đăng ký thất bại - email đã tồn tại")
    void register_duplicateEmail_throwsDuplicateResourceException() {
      when(userRepository.existsByUsername("testuser")).thenReturn(false);
      when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

      assertThatThrownBy(() -> authService.register(registerRequest))
          .isInstanceOf(DuplicateResourceException.class)
          .hasMessageContaining("test@example.com");

      verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Đăng ký - password được encode trước khi lưu")
    void register_passwordIsEncoded_beforeSave() {
      when(userRepository.existsByUsername(any())).thenReturn(false);
      when(userRepository.existsByEmail(any())).thenReturn(false);
      when(userMapper.toEntity(registerRequest)).thenReturn(savedUser);
      when(passwordEncoder.encode("password123")).thenReturn("encoded_pass");
      when(userRepository.save(any(User.class))).thenReturn(savedUser);
      when(userDetailsService.loadUserByUsername(any())).thenReturn(userDetails);
      when(jwtUtil.generateToken(any())).thenReturn("token");

      authService.register(registerRequest);

      verify(passwordEncoder).encode("password123");
    }
  }

  // ───────────────────────────────────────────────────
  // LOGIN
  // ───────────────────────────────────────────────────
  @Nested
  @DisplayName("login()")
  class Login {

    @Test
    @DisplayName("Đăng nhập thành công - trả về JWT token")
    void login_validCredentials_returnsAuthResponse() {
      when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(savedUser));
      when(passwordEncoder.matches("password123", savedUser.getPassword())).thenReturn(true);
      when(userDetailsService.loadUserByUsername("testuser")).thenReturn(userDetails);
      when(jwtUtil.generateToken(userDetails)).thenReturn("jwt_token_456");

      AuthResponse result = authService.login(loginRequest);

      assertThat(result).isNotNull();
      assertThat(result.getToken()).isEqualTo("jwt_token_456");
      assertThat(result.getUsername()).isEqualTo("testuser");
    }

    @Test
    @DisplayName("Đăng nhập thất bại - username không tồn tại")
    void login_usernameNotFound_throwsBadRequestException() {
      when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

      assertThatThrownBy(() -> authService.login(loginRequest))
          .isInstanceOf(BadRequestException.class)
          .hasMessageContaining("Invalid username or password");
    }

    @Test
    @DisplayName("Đăng nhập thất bại - sai mật khẩu")
    void login_wrongPassword_throwsBadRequestException() {
      when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(savedUser));
      when(passwordEncoder.matches("password123", savedUser.getPassword())).thenReturn(false);

      assertThatThrownBy(() -> authService.login(loginRequest))
          .isInstanceOf(BadRequestException.class)
          .hasMessageContaining("Invalid username or password");
    }

    @Test
    @DisplayName("Đăng nhập thất bại - tài khoản bị khóa (banned)")
    void login_bannedUser_throwsBadRequestException() {
      User bannedUser = User.builder()
          .username("testuser")
          .email("test@example.com")
          .role(Role.USER)
          .banned(true)
          .build();
      bannedUser.setId("user123");

      when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(bannedUser));
      when(passwordEncoder.matches("password123", bannedUser.getPassword())).thenReturn(true);

      assertThatThrownBy(() -> authService.login(loginRequest))
          .isInstanceOf(BadRequestException.class)
          .hasMessageContaining("khóa");
    }
  }
}
