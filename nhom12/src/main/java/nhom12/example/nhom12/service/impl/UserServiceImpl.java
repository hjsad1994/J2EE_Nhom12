package nhom12.example.nhom12.service.impl;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import nhom12.example.nhom12.dto.request.ChangePasswordRequest;
import nhom12.example.nhom12.dto.request.CreateUserRequest;
import nhom12.example.nhom12.dto.request.SetupPasswordRequest;
import nhom12.example.nhom12.dto.response.UserResponse;
import nhom12.example.nhom12.exception.BadRequestException;
import nhom12.example.nhom12.exception.DuplicateResourceException;
import nhom12.example.nhom12.exception.ResourceNotFoundException;
import nhom12.example.nhom12.mapper.UserMapper;
import nhom12.example.nhom12.model.User;
import nhom12.example.nhom12.model.enums.Role;
import nhom12.example.nhom12.repository.UserRepository;
import nhom12.example.nhom12.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final UserMapper userMapper;
  private final PasswordEncoder passwordEncoder;
  private final SimpMessagingTemplate messagingTemplate;

  @Override
  public UserResponse createUser(CreateUserRequest request) {
    if (userRepository.existsByUsername(request.getUsername())) {
      throw new DuplicateResourceException("User", "username", request.getUsername());
    }
    if (userRepository.existsByEmail(request.getEmail())) {
      throw new DuplicateResourceException("User", "email", request.getEmail());
    }

    User user = userMapper.toEntity(request);
    user.setPassword(passwordEncoder.encode(request.getPassword()));
    user.setHasPassword(true);
    User saved = userRepository.save(user);
    return userMapper.toResponse(saved);
  }

  @Override
  public UserResponse getUserById(String id) {
    User user =
        userRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    return userMapper.toResponse(user);
  }

  @Override
  public UserResponse getMyProfile(String userId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    return userMapper.toResponse(user);
  }

  @Override
  public void changePassword(String userId, ChangePasswordRequest request) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

    if (!user.isHasPassword()) {
      throw new BadRequestException(
          "Tài khoản chưa có mật khẩu. Vui lòng sử dụng chức năng 'Thiết lập mật khẩu'.");
    }

    if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
      throw new BadRequestException("Mật khẩu hiện tại không đúng");
    }

    user.setPassword(passwordEncoder.encode(request.getNewPassword()));
    userRepository.save(user);
  }

  @Override
  public void setupPassword(String userId, SetupPasswordRequest request) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

    if (user.isHasPassword()) {
      throw new BadRequestException(
          "Tài khoản đã có mật khẩu. Vui lòng sử dụng chức năng 'Đổi mật khẩu'.");
    }

    user.setPassword(passwordEncoder.encode(request.getNewPassword()));
    user.setHasPassword(true);
    userRepository.save(user);
  }

  @Override
  public Page<UserResponse> getAllUsers(Pageable pageable) {
    return userRepository.findAll(pageable).map(userMapper::toResponse);
  }

  @Override
  public UserResponse updateRole(String id, Role role) {
    User user =
        userRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

    // Prevent demoting the last admin (avoid total lockout)
    if (user.getRole() == Role.ADMIN && role == Role.USER) {
      long adminCount = userRepository.countByRole(Role.ADMIN);
      if (adminCount <= 1) {
        throw new BadRequestException(
            "Không thể hạ quyền admin cuối cùng. Hệ thống cần ít nhất một admin.");
      }
    }

    Role oldRole = user.getRole();
    user.setRole(role);
    UserResponse response = userMapper.toResponse(userRepository.save(user));

    // Notify the affected user in real-time via WebSocket
    if (oldRole != role) {
      messagingTemplate.convertAndSend(
          "/topic/role-change/" + id,
          Map.of("userId", id, "oldRole", oldRole.name(), "newRole", role.name()));
    }

    return response;
  }

  @Override
  public void resetPassword(String userId, String newPassword) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    user.setPassword(passwordEncoder.encode(newPassword));
    user.setHasPassword(true);
    userRepository.save(user);
  }
}
