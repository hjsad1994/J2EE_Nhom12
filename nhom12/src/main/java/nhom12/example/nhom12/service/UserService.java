package nhom12.example.nhom12.service;

import nhom12.example.nhom12.dto.request.ChangePasswordRequest;
import nhom12.example.nhom12.dto.request.CreateUserRequest;
import nhom12.example.nhom12.dto.request.SetupPasswordRequest;
import nhom12.example.nhom12.dto.response.UserResponse;
import nhom12.example.nhom12.model.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

  UserResponse createUser(CreateUserRequest request);

  UserResponse getUserById(String id);

  UserResponse getMyProfile(String userId);

  void changePassword(String userId, ChangePasswordRequest request);

  void setupPassword(String userId, SetupPasswordRequest request);

  Page<UserResponse> getAllUsers(Pageable pageable);

  UserResponse updateRole(String id, Role role);

  void resetPassword(String userId, String newPassword);
}
