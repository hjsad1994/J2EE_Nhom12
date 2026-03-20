package nhom12.example.nhom12.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import nhom12.example.nhom12.dto.request.ChangePasswordRequest;
import nhom12.example.nhom12.dto.request.CreateUserRequest;
import nhom12.example.nhom12.dto.request.SetupPasswordRequest;
import nhom12.example.nhom12.dto.response.ApiResponse;
import nhom12.example.nhom12.dto.response.UserResponse;
import nhom12.example.nhom12.exception.ResourceNotFoundException;
import nhom12.example.nhom12.model.User;
import nhom12.example.nhom12.model.enums.Role;
import nhom12.example.nhom12.repository.UserRepository;
import nhom12.example.nhom12.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Validated
public class UserController {

  private final UserService userService;
  private final UserRepository userRepository;

  @PostMapping
  public ResponseEntity<ApiResponse<UserResponse>> createUser(
      @Valid @RequestBody CreateUserRequest request) {
    UserResponse user = userService.createUser(request);
    return new ResponseEntity<>(
        ApiResponse.created(user, "User created successfully"), HttpStatus.CREATED);
  }

  @GetMapping("/me")
  public ResponseEntity<ApiResponse<UserResponse>> getMyProfile(
      @AuthenticationPrincipal UserDetails principal) {
    String userId = resolveUserId(principal);
    return ResponseEntity.ok(
        ApiResponse.success(userService.getMyProfile(userId), "Profile retrieved successfully"));
  }

  @PutMapping("/me/password")
  public ResponseEntity<ApiResponse<Void>> changePassword(
      @AuthenticationPrincipal UserDetails principal,
      @Valid @RequestBody ChangePasswordRequest request) {
    String userId = resolveUserId(principal);
    userService.changePassword(userId, request);
    return ResponseEntity.ok(ApiResponse.success(null, "Password changed successfully"));
  }

  @PostMapping("/me/setup-password")
  public ResponseEntity<ApiResponse<Void>> setupPassword(
      @AuthenticationPrincipal UserDetails principal,
      @Valid @RequestBody SetupPasswordRequest request) {
    String userId = resolveUserId(principal);
    userService.setupPassword(userId, request);
    return ResponseEntity.ok(ApiResponse.success(null, "Password setup successfully"));
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable String id) {
    UserResponse user = userService.getUserById(id);
    return ResponseEntity.ok(ApiResponse.success(user, "User retrieved successfully"));
  }

  @GetMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<Page<UserResponse>>> getAllUsers(
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size) {
    size = Math.min(size, 100);
    Page<UserResponse> users = userService.getAllUsers(PageRequest.of(page, size));
    return ResponseEntity.ok(ApiResponse.success(users, "Users retrieved successfully"));
  }

  @PatchMapping("/{id}/ban")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<UserResponse>> toggleBan(
      @PathVariable String id, @AuthenticationPrincipal UserDetails principal) {
    String requesterId = resolveUserId(principal);
    if (requesterId.equals(id)) {
      throw new nhom12.example.nhom12.exception.BadRequestException(
          "Không thể tự khóa tài khoản của chính mình");
    }
    return ResponseEntity.ok(
        ApiResponse.success(userService.toggleBan(id), "Ban status updated successfully"));
  }

  @PatchMapping("/{id}/role")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<UserResponse>> updateRole(
      @PathVariable String id,
      @RequestParam Role role,
      @AuthenticationPrincipal UserDetails principal) {
    String requesterId = resolveUserId(principal);
    if (requesterId.equals(id)) {
      throw new nhom12.example.nhom12.exception.BadRequestException(
          "Không thể tự thay đổi quyền của chính mình");
    }
    return ResponseEntity.ok(
        ApiResponse.success(userService.updateRole(id, role), "Role updated successfully"));
  }

  private String resolveUserId(UserDetails principal) {
    User user =
        userRepository
            .findByUsername(principal.getUsername())
            .orElseThrow(
                () -> new ResourceNotFoundException("User", "username", principal.getUsername()));
    return user.getId();
  }
}
