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
public class ProductResponse {

  private String id;
  private String name;
  private String brand;
  private String categoryId;
  private String categoryName;
  private double price;
  private Double originalPrice;
  private String image;
  private double rating;
  private String badge;
  private String specs;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
