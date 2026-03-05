package nhom12.example.nhom12.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nhom12.example.nhom12.model.OrderItem;
import nhom12.example.nhom12.model.enums.OrderStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {

  private String id;
  private String userId;
  private String email;
  private String customerName;
  private String phone;
  private String address;
  private String city;
  private String district;
  private String ward;
  private String note;
  private String paymentMethod;
  private OrderStatus status;
  private List<OrderItem> items;
  private double subtotal;
  private double shippingFee;
  private double total;
  private LocalDateTime createdAt;
  private String paymentStatus;
  private String momoTransId;
}
