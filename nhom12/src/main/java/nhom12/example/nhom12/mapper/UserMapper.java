package nhom12.example.nhom12.mapper;

import nhom12.example.nhom12.dto.request.CreateUserRequest;
import nhom12.example.nhom12.dto.response.UserResponse;
import nhom12.example.nhom12.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

  public User toEntity(CreateUserRequest request) {
    return User.builder().username(request.getUsername()).email(request.getEmail()).build();
  }

  public UserResponse toResponse(User user) {
    String authProvider;
    boolean isGoogle = user.getGoogleId() != null && !user.getGoogleId().isBlank();
    if (isGoogle && user.isHasPassword()) {
      authProvider = "GOOGLE_AND_LOCAL";
    } else if (isGoogle) {
      authProvider = "GOOGLE";
    } else {
      authProvider = "LOCAL";
    }

    return UserResponse.builder()
        .id(user.getId())
        .username(user.getUsername())
        .email(user.getEmail())
        .role(user.getRole())
        .hasPassword(user.isHasPassword())
        .authProvider(authProvider)
        .createdAt(user.getCreatedAt())
        .updatedAt(user.getUpdatedAt())
        .build();
  }
}
