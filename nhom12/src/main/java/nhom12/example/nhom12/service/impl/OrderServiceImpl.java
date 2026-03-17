package nhom12.example.nhom12.service.impl;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
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
import nhom12.example.nhom12.service.EmailService;
import nhom12.example.nhom12.service.OrderService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

  private static final int MIN_ORDER_ITEM_QUANTITY = 1;
  private static final int MAX_ORDER_ITEM_QUANTITY = 100;

  /** Valid status transitions: key = current status, value = allowed next statuses. */
  private static final Map<OrderStatus, Set<OrderStatus>> VALID_TRANSITIONS;

  static {
    VALID_TRANSITIONS = new EnumMap<>(OrderStatus.class);
    VALID_TRANSITIONS.put(
        OrderStatus.PENDING, Set.of(OrderStatus.CONFIRMED, OrderStatus.CANCELLED));
    VALID_TRANSITIONS.put(
        OrderStatus.CONFIRMED, Set.of(OrderStatus.SHIPPING, OrderStatus.CANCELLED));
    VALID_TRANSITIONS.put(OrderStatus.SHIPPING, Set.of(OrderStatus.DELIVERED));
    VALID_TRANSITIONS.put(OrderStatus.DELIVERED, Set.of()); // terminal
    VALID_TRANSITIONS.put(OrderStatus.CANCELLED, Set.of()); // terminal
  }

  private final OrderRepository orderRepository;
  private final ProductRepository productRepository;
  private final SimpMessagingTemplate messagingTemplate;
  private final EmailService emailService;

  /**
   * Creates an order atomically:
   *
   * <ol>
   *   <li>Validate and deduct stock using Optimistic Locking (@Version on Product).
   *   <li>Persist order in the same MongoDB transaction.
   *   <li>If any step fails, the entire transaction rolls back (stock is restored automatically).
   *   <li>Duplicate orderCode (idempotency key) is rejected with a user-friendly error.
   * </ol>
   */
  @Override
  @Transactional
  public OrderResponse createOrder(String userId, CreateOrderRequest request) {
    // Use client-supplied idempotency key or generate a server-side UUID
    String orderCode =
        (request.getIdempotencyKey() != null && !request.getIdempotencyKey().isBlank())
            ? request.getIdempotencyKey()
            : UUID.randomUUID().toString();

    List<OrderItem> items = new ArrayList<>();

    // Single-pass: validate stock, deduct, and build order items
    // Optimistic Locking: productRepository.save() throws OptimisticLockingFailureException
    // if the product's @Version field was modified by a concurrent request since we loaded it.
    try {
      for (var itemReq : request.getItems()) {
        Product product =
            productRepository
                .findById(itemReq.getProductId())
                .orElseThrow(
                    () -> new ResourceNotFoundException("Product", "id", itemReq.getProductId()));

        if (product.getStock() < itemReq.getQuantity()) {
          throw new BadRequestException(
              "Sản phẩm '"
                  + product.getName()
                  + "' chỉ còn "
                  + product.getStock()
                  + " sản phẩm trong kho");
        }

        // Deduct stock — if a concurrent order modified this product between our read and this
        // save, MongoDB will detect the @Version mismatch and throw
        // OptimisticLockingFailureException
        product.setStock(product.getStock() - itemReq.getQuantity());
        productRepository.save(product);

        items.add(buildOrderItem(product, itemReq.getQuantity()));
      }
    } catch (OptimisticLockingFailureException e) {
      // Another request updated the product's stock concurrently (e.g., Flash Sale)
      throw new BadRequestException("Sản phẩm vừa được cập nhật bởi người khác. Vui lòng thử lại.");
    }

    double subtotal = items.stream().mapToDouble(i -> i.getPrice() * i.getQuantity()).sum();
    double shippingFee = subtotal >= 500000 ? 0 : 30000;
    double total = subtotal + shippingFee;

    Order order =
        Order.builder()
            .userId(userId)
            .orderCode(orderCode)
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

    // DuplicateKeyException: orderCode unique index blocks duplicate submissions.
    // This catches cases where the same idempotency key is reused (client retry on network error).
    try {
      Order saved = orderRepository.save(order);
      // Send confirmation email asynchronously (@Async) — does not block or affect this transaction
      emailService.sendOrderConfirmationEmail(
          saved.getEmail(), saved.getCustomerName(), saved.getOrderCode(), saved.getTotal());
      return toResponse(saved);
    } catch (DuplicateKeyException e) {
      throw new BadRequestException(
          "Đơn hàng với mã '" + orderCode + "' đã tồn tại. Yêu cầu này đã được xử lý trước đó.");
    }
  }

  private OrderItem buildOrderItem(Product product, int quantity) {
    if (quantity < MIN_ORDER_ITEM_QUANTITY || quantity > MAX_ORDER_ITEM_QUANTITY) {
      throw new BadRequestException(
          "Order item quantity must be between "
              + MIN_ORDER_ITEM_QUANTITY
              + " and "
              + MAX_ORDER_ITEM_QUANTITY);
    }

    return OrderItem.builder()
        .productId(product.getId())
        .productName(product.getName())
        .productImage(product.getImage())
        .brand(product.getBrand())
        .price(product.getPrice())
        .quantity(quantity)
        .build();
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
  @Transactional
  public OrderResponse updateStatus(String id, OrderStatus newStatus) {
    Order order =
        orderRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));

    OrderStatus currentStatus = order.getStatus();

    // Validate status transition
    Set<OrderStatus> allowed = VALID_TRANSITIONS.getOrDefault(currentStatus, Set.of());
    if (!allowed.contains(newStatus)) {
      throw new BadRequestException(
          "Không thể chuyển trạng thái từ '"
              + currentStatus
              + "' sang '"
              + newStatus
              + "'. Trạng thái hợp lệ: "
              + allowed);
    }

    // Restore stock when order is cancelled (atomic within this transaction)
    if (newStatus == OrderStatus.CANCELLED) {
      restoreStock(order);
      order.setCancelledBy("ADMIN");
      order.setCancelReason("Hủy bởi quản trị viên");
    }

    // Auto-mark COD as PAID when delivered
    if (newStatus == OrderStatus.DELIVERED && "COD".equals(order.getPaymentMethod())) {
      order.setPaymentStatus("PAID");
    }

    order.setStatus(newStatus);
    OrderResponse response = toResponse(orderRepository.save(order));

    // Send WebSocket notification for order status change
    messagingTemplate.convertAndSendToUser(
        order.getUserId(),
        "/queue/order-status",
        Map.of(
            "orderId", order.getId(),
            "oldStatus", currentStatus.name(),
            "newStatus", newStatus.name(),
            "message",
                "Đơn hàng #"
                    + order.getId().substring(order.getId().length() - 6).toUpperCase()
                    + " đã chuyển sang "
                    + newStatus.name()));

    return response;
  }

  @Override
  @Transactional
  public OrderResponse cancelOrder(String orderId, String userId, String cancelReason) {
    Order order =
        orderRepository
            .findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

    // Verify ownership
    if (!order.getUserId().equals(userId)) {
      throw new BadRequestException("Bạn không có quyền hủy đơn hàng này");
    }

    // Allow cancellation of PENDING and CONFIRMED orders (not yet shipped)
    if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.CONFIRMED) {
      throw new BadRequestException(
          "Chỉ có thể hủy đơn hàng chưa được giao cho đơn vị vận chuyển. "
              + "Trạng thái hiện tại: "
              + order.getStatus());
    }

    // Restore stock (atomic within this transaction)
    restoreStock(order);

    order.setStatus(OrderStatus.CANCELLED);
    order.setPaymentStatus("FAILED");
    order.setCancelReason(cancelReason);
    order.setCancelledBy("USER");
    return toResponse(orderRepository.save(order));
  }

  /** Restore product stock when an order is cancelled. Must be called within a transaction. */
  private void restoreStock(Order order) {
    for (OrderItem item : order.getItems()) {
      productRepository
          .findById(item.getProductId())
          .ifPresent(
              product -> {
                product.setStock(product.getStock() + item.getQuantity());
                productRepository.save(product);
              });
    }
  }

  private OrderResponse toResponse(Order o) {
    return OrderResponse.builder()
        .id(o.getId())
        .orderCode(o.getOrderCode())
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
        .cancelReason(o.getCancelReason())
        .cancelledBy(o.getCancelledBy())
        .build();
  }
}
