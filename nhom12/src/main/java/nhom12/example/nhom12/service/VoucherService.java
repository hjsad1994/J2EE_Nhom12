package nhom12.example.nhom12.service;

import java.util.List;
import nhom12.example.nhom12.dto.request.CreateVoucherRequest;
import nhom12.example.nhom12.dto.request.CreateOrderRequest;
import nhom12.example.nhom12.dto.request.UpdateVoucherRequest;
import nhom12.example.nhom12.dto.response.VoucherResponse;
import nhom12.example.nhom12.dto.response.VoucherValidationResponse;
import nhom12.example.nhom12.model.Order;

public interface VoucherService {

  VoucherResponse createVoucher(CreateVoucherRequest request);

  VoucherResponse updateVoucher(String id, UpdateVoucherRequest request);

  void deleteVoucher(String id);

  List<VoucherResponse> getAllVouchers();

  List<VoucherResponse> getAvailableVouchers(List<CreateOrderRequest.OrderItemRequest> items);

  VoucherValidationResponse validateOrderVouchers(
      List<CreateOrderRequest.OrderItemRequest> items, String productVoucherCode, String shippingVoucherCode);

  void markOrderVoucherUsage(Order order);

  void rollbackOrderVoucherUsage(Order order);
}
