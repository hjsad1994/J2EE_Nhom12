package nhom12.example.nhom12.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Embedded cart item — not a standalone MongoDB document. */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {
  private String productId;
  private String productName;
  private String productImage;
  private String brand;
  private double price;
  private int quantity;
}
