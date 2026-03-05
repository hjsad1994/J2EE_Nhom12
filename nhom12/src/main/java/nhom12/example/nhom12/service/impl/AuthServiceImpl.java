package nhom12.example.nhom12.service.impl;

import lombok.RequiredArgsConstructor;
import nhom12.example.nhom12.dto.request.CreateUserRequest;
import nhom12.example.nhom12.dto.request.LoginRequest;
import nhom12.example.nhom12.dto.response.AuthResponse;
import nhom12.example.nhom12.exception.BadRequestException;
import nhom12.example.nhom12.exception.DuplicateResourceException;
import nhom12.example.nhom12.mapper.UserMapper;
import nhom12.example.nhom12.model.User;
import nhom12.example.nhom12.repository.UserRepository;
import nhom12.example.nhom12.security.CustomUserDetailsService;
import nhom12.example.nhom12.security.JwtUtil;
import nhom12.example.nhom12.service.AuthService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

  private final UserRepository userRepository;
  private final UserMapper userMapper;
  private final PasswordEncoder passwordEncoder;
  private final JwtUtil jwtUtil;
  private final CustomUserDetailsService userDetailsService;

  @Override
  public AuthResponse register(CreateUserRequest request) {
    if (userRepository.existsByUsername(request.getUsername())) {
      throw new DuplicateResourceException("User", "username", request.getUsername());
    }
    if (userRepository.existsByEmail(request.getEmail())) {
      throw new DuplicateResourceException("User", "email", request.getEmail());
    }

    User user = userMapper.toEntity(request);
    user.setPassword(passwordEncoder.encode(request.getPassword()));
    User saved = userRepository.save(user);

    UserDetails userDetails = userDetailsService.loadUserByUsername(saved.getUsername());
    String token = jwtUtil.generateToken(userDetails);

    return AuthResponse.builder()
        .token(token)
        .id(saved.getId())
        .username(saved.getUsername())
        .email(saved.getEmail())
        .role(saved.getRole())
        .build();
  }

  @Override
  public AuthResponse login(LoginRequest request) {
    User user =
        userRepository
            .findByUsername(request.getUsername())
            .orElseThrow(() -> new BadRequestException("Invalid username or password"));

    if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
      throw new BadRequestException("Invalid username or password");
    }

    UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
    String token = jwtUtil.generateToken(userDetails);

    return AuthResponse.builder()
        .token(token)
        .id(user.getId())
        .username(user.getUsername())
        .email(user.getEmail())
        .role(user.getRole())
        .build();
  }
}
