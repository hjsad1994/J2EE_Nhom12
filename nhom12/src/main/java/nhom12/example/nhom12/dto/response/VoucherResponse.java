package nhom12.example.nhom12.dto.response;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nhom12.example.nhom12.model.enums.VoucherDiscountType;
import nhom12.example.nhom12.model.enums.VoucherType;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VoucherResponse {
  private String id;
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
  private boolean usable;
}
