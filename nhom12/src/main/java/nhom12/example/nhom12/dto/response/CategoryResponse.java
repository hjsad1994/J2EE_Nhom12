package nhom12.example.nhom12.dto.response;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryResponse {

  private String id;
  private String name;
  private String slug;
  private String description;
  private String icon;
  private LocalDateTime createdAt;
}
