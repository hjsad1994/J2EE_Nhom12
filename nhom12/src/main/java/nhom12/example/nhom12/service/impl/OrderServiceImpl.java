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
import nhom12.example.nhom12.dto.response.VoucherValidationResponse;
import nhom12.example.nhom12.exception.BadRequestException;
import nhom12.example.nhom12.exception.ResourceNotFoundException;
import nhom12.example.nhom12.model.Order;
import nhom12.example.nhom12.model.OrderItem;
import nhom12.example.nhom12.model.Product;
import nhom12.example.nhom12.model.ProductVariant;
import nhom12.example.nhom12.model.enums.OrderStatus;
import nhom12.example.nhom12.repository.OrderRepository;
import nhom12.example.nhom12.repository.ProductRepository;
import nhom12.example.nhom12.service.EmailService;
import nhom12.example.nhom12.service.OrderService;
import nhom12.example.nhom12.service.VoucherService;
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
    VALID_TRANSITIONS.put(OrderStatus.DELIVERED, Set.of());
    VALID_TRANSITIONS.put(OrderStatus.CANCELLED, Set.of());
  }

  private final OrderRepository orderRepository;
  private final ProductRepository productRepository;
  private final SimpMessagingTemplate messagingTemplate;
  private final EmailService emailService;
  private final VoucherService voucherService;

  @Override
  @Transactional
  public OrderResponse createOrder(String userId, CreateOrderRequest request) {
    String orderCode =
        (request.getIdempotencyKey() != null && !request.getIdempotencyKey().isBlank())
            ? request.getIdempotencyKey()
            : UUID.randomUUID().toString();

    List<OrderItem> items = new ArrayList<>();

    try {
      for (var itemReq : request.getItems()) {
        Product product =
            productRepository
                .findById(itemReq.getProductId())
                .orElseThrow(
                    () -> new ResourceNotFoundException("Product", "id", itemReq.getProductId()));
        ProductVariant variant = findVariant(product, itemReq.getColor(), itemReq.getStorage());
        int availableStock = variant != null ? variant.getStock() : product.getStock();

        if (availableStock < itemReq.getQuantity()) {
          throw new BadRequestException(
              "Sản phẩm '"
                  + product.getName()
                  + "' chỉ còn "
                  + availableStock
                  + " sản phẩm trong kho");
        }

        if (variant != null) {
          variant.setStock(variant.getStock() - itemReq.getQuantity());
          syncSummaryFieldsFromVariants(product);
        } else {
          product.setStock(product.getStock() - itemReq.getQuantity());
        }

        productRepository.save(product);
        items.add(buildOrderItem(product, itemReq, variant));
      }
    } catch (OptimisticLockingFailureException e) {
      throw new BadRequestException(
          "Sản phẩm vừa được cập nhật bởi người khác. Vui lòng thử lại.");
    }

    VoucherValidationResponse voucherSummary =
        voucherService.validateOrderVouchers(
            request.getItems(), request.getProductVoucherCode(), request.getShippingVoucherCode());

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
            .subtotal(voucherSummary.getSubtotal())
            .productDiscount(voucherSummary.getProductDiscount())
            .shippingDiscount(voucherSummary.getShippingDiscount())
            .discountTotal(voucherSummary.getTotalDiscount())
            .originalShippingFee(voucherSummary.getOriginalShippingFee())
            .shippingFee(voucherSummary.getShippingFee())
            .total(voucherSummary.getTotal())
            .productVoucher(voucherSummary.getProductVoucher())
            .shippingVoucher(voucherSummary.getShippingVoucher())
            .build();

    try {
      Order saved = orderRepository.save(order);
      voucherService.markOrderVoucherUsage(saved);
      if (!"MOMO".equalsIgnoreCase(saved.getPaymentMethod())) {
        emailService.sendOrderConfirmationEmail(
            saved.getEmail(), saved.getCustomerName(), saved.getOrderCode(), saved.getTotal());
      }
      return toResponse(saved);
    } catch (DuplicateKeyException e) {
      return orderRepository
          .findByOrderCode(orderCode)
          .map(this::toResponse)
          .orElseThrow(
              () ->
                  new BadRequestException(
                      "Đơn hàng với mã '"
                          + orderCode
                          + "' đã tồn tại. Yêu cầu này đã được xử lý trước đó."));
    }
  }

  private OrderItem buildOrderItem(
      Product product, CreateOrderRequest.OrderItemRequest itemRequest, ProductVariant variant) {
    int quantity = itemRequest.getQuantity();
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
        .productImage(resolveOrderImage(product, variant))
        .brand(product.getBrand())
        .color(normalizeOption(itemRequest.getColor()))
        .storage(normalizeOption(itemRequest.getStorage()))
        .price(variant != null ? variant.getPrice() : product.getPrice())
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

    if (newStatus == OrderStatus.CANCELLED) {
      if ("PAID".equals(order.getPaymentStatus())) {
        throw new BadRequestException("Đơn hàng đã thanh toán. Hãy hoàn tiền trước khi hủy.");
      }
      restoreStock(order);
      voucherService.rollbackOrderVoucherUsage(order);
      order.setPaymentStatus("FAILED");
      order.setCancelledBy("ADMIN");
      order.setCancelReason("Hủy bởi quản trị viên");
    }

    if (newStatus == OrderStatus.DELIVERED && "COD".equals(order.getPaymentMethod())) {
      order.setPaymentStatus("PAID");
    }

    order.setStatus(newStatus);
    OrderResponse response = toResponse(orderRepository.save(order));

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

    if (!order.getUserId().equals(userId)) {
      throw new BadRequestException("Bạn không có quyền hủy đơn hàng này");
    }

    if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.CONFIRMED) {
      throw new BadRequestException(
          "Chỉ có thể hủy đơn hàng chưa được giao cho đơn vị vận chuyển. Trạng thái hiện tại: "
              + order.getStatus());
    }

    if ("PAID".equals(order.getPaymentStatus())) {
      throw new BadRequestException("Đơn hàng đã thanh toán và cần hoàn tiền trước khi hủy.");
    }

    restoreStock(order);
    voucherService.rollbackOrderVoucherUsage(order);

    order.setStatus(OrderStatus.CANCELLED);
    order.setPaymentStatus("FAILED");
    order.setCancelReason(cancelReason);
    order.setCancelledBy("USER");
    return toResponse(orderRepository.save(order));
  }

  private void restoreStock(Order order) {
    for (OrderItem item : order.getItems()) {
      productRepository
          .findById(item.getProductId())
          .ifPresent(
              product -> {
                ProductVariant variant = findVariant(product, item.getColor(), item.getStorage());
                if (variant != null) {
                  variant.setStock(variant.getStock() + item.getQuantity());
                  syncSummaryFieldsFromVariants(product);
                } else {
                  product.setStock(product.getStock() + item.getQuantity());
                }
                productRepository.save(product);
              });
    }
  }

  private ProductVariant findVariant(Product product, String color, String storage) {
    String normalizedColor = normalizeOption(color);
    String normalizedStorage = normalizeOption(storage);

    if (normalizedColor.isBlank() && normalizedStorage.isBlank()) {
      return null;
    }

    if (product.getVariants() == null) {
      throw new ResourceNotFoundException(
          "Product variant",
          "productId",
          product.getId() + ":" + normalizedColor + ":" + normalizedStorage);
    }

    return product.getVariants().stream()
        .filter(
            variant ->
                normalizeOption(variant.getColor()).equals(normalizedColor)
                    && normalizeOption(variant.getStorage()).equals(normalizedStorage))
        .findFirst()
        .orElseThrow(
            () ->
                new ResourceNotFoundException(
                    "Product variant",
                    "productId",
                    product.getId() + ":" + normalizedColor + ":" + normalizedStorage));
  }

  private void syncSummaryFieldsFromVariants(Product product) {
    if (product.getVariants() == null || product.getVariants().isEmpty()) {
      return;
    }

    ProductVariant primaryVariant = product.getVariants().get(0);
    product.setPrice(primaryVariant.getPrice());
    product.setStock(product.getVariants().stream().mapToInt(ProductVariant::getStock).sum());
  }

  private String resolveOrderImage(Product product, ProductVariant variant) {
    if (variant != null && variant.getImage() != null && !variant.getImage().isBlank()) {
      return variant.getImage();
    }
    return product.getImage();
  }

  private String normalizeOption(String value) {
    return value == null ? "" : value.trim();
  }

  private OrderResponse toResponse(Order order) {
    return OrderResponse.builder()
        .id(order.getId())
        .orderCode(order.getOrderCode())
        .userId(order.getUserId())
        .email(order.getEmail())
        .customerName(order.getCustomerName())
        .phone(order.getPhone())
        .address(order.getAddress())
        .city(order.getCity())
        .district(order.getDistrict())
        .ward(order.getWard())
        .note(order.getNote())
        .paymentMethod(order.getPaymentMethod())
        .status(order.getStatus())
        .items(order.getItems())
        .subtotal(order.getSubtotal())
        .productDiscount(order.getProductDiscount())
        .shippingDiscount(order.getShippingDiscount())
        .discountTotal(order.getDiscountTotal())
        .originalShippingFee(order.getOriginalShippingFee())
        .shippingFee(order.getShippingFee())
        .total(order.getTotal())
        .productVoucher(order.getProductVoucher())
        .shippingVoucher(order.getShippingVoucher())
        .createdAt(order.getCreatedAt())
        .paymentStatus(order.getPaymentStatus())
        .momoTransId(order.getMomoTransId())
        .cancelReason(order.getCancelReason())
        .cancelledBy(order.getCancelledBy())
        .build();
  }
}
