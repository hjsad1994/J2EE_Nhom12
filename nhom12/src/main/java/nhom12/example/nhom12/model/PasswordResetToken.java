package nhom12.example.nhom12.model;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "password_reset_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class PasswordResetToken extends BaseDocument {

  @Indexed private String userId;

  @Indexed(unique = true)
  private String token;

  private LocalDateTime expiresAt;

  public boolean isExpired() {
    return LocalDateTime.now().isAfter(expiresAt);
  }
}
