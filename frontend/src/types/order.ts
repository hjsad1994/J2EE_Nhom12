export type OrderStatus =
  | 'PENDING'
  | 'CONFIRMED'
  | 'SHIPPING'
  | 'DELIVERED'
  | 'CANCELLED';

export interface OrderItem {
  productId: string;
  productName: string;
  productImage: string;
  brand: string;
  price: number;
  quantity: number;
}

export interface Order {
  id: string;
  userId: string;
  email: string;
  customerName: string;
  phone: string;
  address: string;
  city: string;
  district: string;
  ward: string;
  note?: string;
  paymentMethod: string;
  status: OrderStatus;
  items: OrderItem[];
  subtotal: number;
  shippingFee: number;
  total: number;
  createdAt: string;
  paymentStatus?: string;
  momoTransId?: string;
}

export interface CreateOrderPayload {
  email: string;
  customerName: string;
  phone: string;
  address: string;
  city: string;
  district: string;
  ward: string;
  note?: string;
  paymentMethod: string;
  items: OrderItem[];
}

export const ORDER_STATUS_LABEL: Record<OrderStatus, string> = {
  PENDING: 'Chờ xác nhận',
  CONFIRMED: 'Đã xác nhận',
  SHIPPING: 'Đang giao',
  DELIVERED: 'Đã giao',
  CANCELLED: 'Đã hủy',
};

export const ORDER_STATUS_COLOR: Record<OrderStatus, string> = {
  PENDING: 'bg-yellow-50 text-yellow-700',
  CONFIRMED: 'bg-blue-50 text-blue-700',
  SHIPPING: 'bg-purple-50 text-purple-700',
  DELIVERED: 'bg-green-50 text-green-700',
  CANCELLED: 'bg-red-50 text-red-700',
};
