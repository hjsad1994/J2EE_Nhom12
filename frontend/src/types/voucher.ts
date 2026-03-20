export type VoucherType = 'PRODUCT' | 'SHIPPING';
export type VoucherDiscountType = 'PERCENTAGE' | 'FIXED_AMOUNT';

export interface AppliedVoucher {
  voucherId: string;
  code: string;
  type: VoucherType;
  discountType: VoucherDiscountType;
  discountValue: number;
  discountAmount: number;
}

export interface Voucher {
  id: string;
  code: string;
  description?: string;
  type: VoucherType;
  discountType: VoucherDiscountType;
  discountValue: number;
  maxDiscountAmount?: number | null;
  minOrderValue: number;
  usageLimit?: number | null;
  usedCount: number;
  startAt?: string | null;
  endAt?: string | null;
  active: boolean;
  usable: boolean;
}

export interface VoucherValidation {
  subtotal: number;
  originalShippingFee: number;
  shippingFee: number;
  productDiscount: number;
  shippingDiscount: number;
  totalDiscount: number;
  total: number;
  productVoucher?: AppliedVoucher | null;
  shippingVoucher?: AppliedVoucher | null;
}

export interface VoucherFormPayload {
  code: string;
  description?: string;
  type: VoucherType;
  discountType: VoucherDiscountType;
  discountValue: number;
  maxDiscountAmount?: number | null;
  minOrderValue: number;
  usageLimit?: number | null;
  startAt?: string | null;
  endAt?: string | null;
  active: boolean;
}
