package nhom12.example.nhom12.model;

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
public class OrderItem {

  private String productId;
  private String productName;
  private String productImage;
  private String brand;
  private double price;
  private int quantity;
}
