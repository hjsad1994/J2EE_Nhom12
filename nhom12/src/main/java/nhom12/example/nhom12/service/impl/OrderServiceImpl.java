package nhom12.example.nhom12.service.impl;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import nhom12.example.nhom12.dto.request.CreateOrderRequest;
import nhom12.example.nhom12.dto.response.OrderResponse;
import nhom12.example.nhom12.exception.BadRequestException;
import nhom12.example.nhom12.exception.ResourceNotFoundException;
import nhom12.example.nhom12.model.Order;
import nhom12.example.nhom12.model.OrderItem;
import nhom12.example.nhom12.model.Product;
import nhom12.example.nhom12.model.enums.OrderStatus;
import nhom12.example.nhom12.repository.OrderRepository;
import nhom12.example.nhom12.repository.ProductRepository;
import nhom12.example.nhom12.service.OrderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

  private final OrderRepository orderRepository;
  private final ProductRepository productRepository;

  @Override
  public OrderResponse createOrder(String userId, CreateOrderRequest request) {
    List<String> productIds =
        request.getItems().stream().map(CreateOrderRequest.OrderItemRequest::getProductId).toList();
    Map<String, Product> productsById =
        productRepository.findAllById(productIds).stream()
            .collect(Collectors.toMap(Product::getId, Function.identity(), (a, b) -> a));

    List<OrderItem> items =
        request.getItems().stream()
            .map(
                i -> {
                  Product product = productsById.get(i.getProductId());
                  if (product == null) {
                    throw new ResourceNotFoundException("Product", "id", i.getProductId());
                  }
                  if (i.getQuantity() <= 0) {
                    throw new BadRequestException("Quantity must be greater than zero");
                  }
                  return OrderItem.builder()
                      .productId(product.getId())
                      .productName(product.getName())
                      .productImage(product.getImage())
                      .brand(product.getBrand())
                      .price(product.getPrice())
                      .quantity(i.getQuantity())
                      .build();
                })
            .toList();

    double subtotal = items.stream().mapToDouble(i -> i.getPrice() * i.getQuantity()).sum();
    double shippingFee = subtotal >= 500000 ? 0 : 30000;
    double total = subtotal + shippingFee;

    Order order =
        Order.builder()
            .userId(userId)
            .email(request.getEmail())
            .customerName(request.getCustomerName())
            .phone(request.getPhone())
            .address(request.getAddress())
            .city(request.getCity())
            .district(request.getDistrict())
            .ward(request.getWard())
            .note(request.getNote())
            .paymentMethod(request.getPaymentMethod())
            .status(OrderStatus.PENDING)
            .paymentStatus("PENDING")
            .items(items)
            .subtotal(subtotal)
            .shippingFee(shippingFee)
            .total(total)
            .build();

    return toResponse(orderRepository.save(order));
  }

  @Override
  public List<OrderResponse> getMyOrders(String userId) {
    return orderRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
        .map(this::toResponse)
        .toList();
  }

  @Override
  public Page<OrderResponse> getAllOrders(Pageable pageable) {
    return orderRepository.findAllByOrderByCreatedAtDesc(pageable).map(this::toResponse);
  }

  @Override
  public OrderResponse updateStatus(String id, OrderStatus status) {
    Order order =
        orderRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));
    order.setStatus(status);
    return toResponse(orderRepository.save(order));
  }

  private OrderResponse toResponse(Order o) {
    return OrderResponse.builder()
        .id(o.getId())
        .userId(o.getUserId())
        .email(o.getEmail())
        .customerName(o.getCustomerName())
        .phone(o.getPhone())
        .address(o.getAddress())
        .city(o.getCity())
        .district(o.getDistrict())
        .ward(o.getWard())
        .note(o.getNote())
        .paymentMethod(o.getPaymentMethod())
        .status(o.getStatus())
        .items(o.getItems())
        .subtotal(o.getSubtotal())
        .shippingFee(o.getShippingFee())
        .total(o.getTotal())
        .createdAt(o.getCreatedAt())
        .paymentStatus(o.getPaymentStatus())
        .momoTransId(o.getMomoTransId())
        .build();
  }
}
