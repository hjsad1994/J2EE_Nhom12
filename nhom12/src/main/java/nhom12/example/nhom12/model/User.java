package nhom12.example.nhom12.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import nhom12.example.nhom12.model.enums.Role;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class User extends BaseDocument {

  @Indexed(unique = true)
  private String username;

  @Indexed(unique = true)
  private String email;

  private String password;

  @Indexed(sparse = true)
  private String googleId;

  @Builder.Default private Role role = Role.USER;

  /** true if user explicitly set their own password (not auto-generated for OAuth). */
  @Builder.Default private boolean hasPassword = false;
}
