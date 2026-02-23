package nhom12.example.nhom12.dto.response;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nhom12.example.nhom12.model.enums.Role;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

  private String id;
  private String username;
  private String email;
  private Role role;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
