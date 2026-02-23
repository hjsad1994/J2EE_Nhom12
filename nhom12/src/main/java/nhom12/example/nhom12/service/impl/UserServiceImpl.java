package nhom12.example.nhom12.service.impl;

import lombok.RequiredArgsConstructor;
import nhom12.example.nhom12.dto.request.CreateUserRequest;
import nhom12.example.nhom12.dto.response.UserResponse;
import nhom12.example.nhom12.exception.DuplicateResourceException;
import nhom12.example.nhom12.exception.ResourceNotFoundException;
import nhom12.example.nhom12.mapper.UserMapper;
import nhom12.example.nhom12.model.User;
import nhom12.example.nhom12.repository.UserRepository;
import nhom12.example.nhom12.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final UserMapper userMapper;
  private final PasswordEncoder passwordEncoder;

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
  public Page<UserResponse> getAllUsers(Pageable pageable) {
    return userRepository.findAll(pageable).map(userMapper::toResponse);
  }
}
