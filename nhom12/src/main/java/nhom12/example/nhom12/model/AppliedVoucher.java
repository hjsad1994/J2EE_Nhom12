package nhom12.example.nhom12.model;

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
public class AppliedVoucher {
  private String voucherId;
  private String code;
  private VoucherType type;
  private VoucherDiscountType discountType;
  private double discountValue;
  private double discountAmount;
}
