package nhom12.example.nhom12.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateProductRequest {

  @NotBlank(message = "Name is required")
  private String name;

  @NotBlank(message = "Brand is required")
  private String brand;

  private String categoryId;

  @NotNull(message = "Price is required")
  @Min(value = 0, message = "Price must be non-negative")
  private Double price;

  private Double originalPrice;

  @NotBlank(message = "Image URL is required")
  private String image;

  private Double rating = 0.0;
  private String badge;
  private String specs;
}
