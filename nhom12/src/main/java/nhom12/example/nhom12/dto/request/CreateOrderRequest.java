package nhom12.example.nhom12.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {

  @NotBlank private String email;

  @NotBlank private String customerName;

  @NotBlank private String phone;

  @NotBlank private String address;

  @NotBlank private String city;

  @NotBlank private String district;

  @NotBlank private String ward;

  private String note;

  @NotBlank private String paymentMethod;

  @NotEmpty private List<OrderItemRequest> items;

  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  public static class OrderItemRequest {
    private String productId;
    private String productName;
    private String productImage;
    private String brand;
    private double price;
    private int quantity;
  }
}
