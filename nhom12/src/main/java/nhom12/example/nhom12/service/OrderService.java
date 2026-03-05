package nhom12.example.nhom12.service;

import java.util.List;
import nhom12.example.nhom12.dto.request.CreateOrderRequest;
import nhom12.example.nhom12.dto.response.OrderResponse;
import nhom12.example.nhom12.model.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {

  OrderResponse createOrder(String userId, CreateOrderRequest request);

  List<OrderResponse> getMyOrders(String userId);

  Page<OrderResponse> getAllOrders(Pageable pageable);

  OrderResponse updateStatus(String id, OrderStatus status);
}
