package nhom12.example.nhom12.model;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import nhom12.example.nhom12.model.enums.OrderStatus;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Order extends BaseDocument {

  @Indexed private String userId;

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

  // Payment tracking
  private String paymentStatus; // PENDING, PAID, FAILED
  private String momoTransId;
}
