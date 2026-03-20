package nhom12.example.nhom12.model;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import nhom12.example.nhom12.model.enums.VoucherDiscountType;
import nhom12.example.nhom12.model.enums.VoucherType;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "vouchers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Voucher extends BaseDocument {

  @Indexed(unique = true)
  private String code;

  private String description;
  private VoucherType type;
  private VoucherDiscountType discountType;
  private double discountValue;
  private Double maxDiscountAmount;
  private double minOrderValue;
  private Integer usageLimit;
  private int usedCount;
  private LocalDateTime startAt;
  private LocalDateTime endAt;
  private boolean active;
}
