package nhom12.example.nhom12.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateReviewRequest {

  @NotBlank(message = "Product ID is required")
  private String productId;

  @NotNull(message = "Rating is required")
  @Min(value = 1, message = "Rating must be between 1 and 5")
  @Max(value = 5, message = "Rating must be between 1 and 5")
  private Integer rating;

  @NotBlank(message = "Comment is required")
  @Size(max = 1000, message = "Comment must not exceed 1000 characters")
  private String comment;
}
