package nhom12.example.nhom12.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nhom12.example.nhom12.model.enums.VoucherDiscountType;
import nhom12.example.nhom12.model.enums.VoucherType;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateVoucherRequest {

  @NotBlank private String code;
  private String description;
  @NotNull private VoucherType type;
  @NotNull private VoucherDiscountType discountType;
  @DecimalMin("0.0")
  private double discountValue;
  @DecimalMin("0.0")
  private Double maxDiscountAmount;
  @DecimalMin("0.0")
  private double minOrderValue;
  private Integer usageLimit;
  private LocalDateTime startAt;
  private LocalDateTime endAt;
  private boolean active;
}
