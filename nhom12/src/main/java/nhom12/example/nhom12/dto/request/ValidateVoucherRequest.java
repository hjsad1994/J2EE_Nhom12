package nhom12.example.nhom12.dto.request;

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
public class ValidateVoucherRequest {
  @NotEmpty private List<CreateOrderRequest.OrderItemRequest> items;
  private String productVoucherCode;
  private String shippingVoucherCode;
}
