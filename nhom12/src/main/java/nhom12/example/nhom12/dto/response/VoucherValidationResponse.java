package nhom12.example.nhom12.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nhom12.example.nhom12.model.AppliedVoucher;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VoucherValidationResponse {
  private double subtotal;
  private double originalShippingFee;
  private double shippingFee;
  private double productDiscount;
  private double shippingDiscount;
  private double totalDiscount;
  private double total;
  private AppliedVoucher productVoucher;
  private AppliedVoucher shippingVoucher;
}
