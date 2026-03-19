package nhom12.example.nhom12.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import nhom12.example.nhom12.dto.request.CreateOrderRequest;
import nhom12.example.nhom12.dto.response.OrderResponse;
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
import nhom12.example.nhom12.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderServiceImpl - Unit Tests")
class OrderServiceImplTest {

  @Mock private OrderRepository orderRepository;
  @Mock private ProductRepository productRepository;
  @Mock private SimpMessagingTemplate messagingTemplate;
  @Mock private EmailService emailService;

  @InjectMocks private OrderServiceImpl orderService;

  private static final String USER_ID = "user001";
  private static final String PRODUCT_ID = "prod001";
  private static final String ORDER_ID = "order001";

  private Product product;
  private Product productWithVariant;
  private ProductVariant variant;
  private CreateOrderRequest orderRequest;

  @BeforeEach
  void setUp() {
    product = new Product();
    product.setId(PRODUCT_ID);
    product.setName("Samsung Galaxy S25");
    product.setBrand("Samsung");
    product.setImage("samsung.jpg");
    product.setPrice(15000000);
    product.setStock(10);

    variant = ProductVariant.builder()
        .color("Blue")
        .storage("256GB")
        .price(18000000)
        .stock(5)
        .image("samsung-blue.jpg")
        .build();

    productWithVariant = new Product();
    productWithVariant.setId(PRODUCT_ID);
    productWithVariant.setName("Samsung Galaxy S25");
    productWithVariant.setBrand("Samsung");
    productWithVariant.setImage("samsung.jpg");
    productWithVariant.setPrice(18000000);
    productWithVariant.setVariants(new ArrayList<>(List.of(variant)));

    CreateOrderRequest.OrderItemRequest itemReq = new CreateOrderRequest.OrderItemRequest(
        PRODUCT_ID, "Samsung Galaxy S25", "samsung.jpg", "Samsung", null, null, 15000000, 2);

    orderRequest = new CreateOrderRequest();
    orderRequest.setEmail("customer@example.com");
    orderRequest.setCustomerName("Nguyen Van A");
    orderRequest.setPhone("0901234567");
    orderRequest.setAddress("123 Le Loi");
    orderRequest.setCity("Ho Chi Minh");
    orderRequest.setDistrict("Quan 1");
    orderRequest.setWard("Phuong Ben Nghe");
    orderRequest.setPaymentMethod("COD");
    orderRequest.setItems(List.of(itemReq));
  }

  // ───────────────────────────────────────────────────
  // CREATE ORDER
  // ───────────────────────────────────────────────────
  @Nested
  @DisplayName("createOrder()")
  class CreateOrder {

    @Test
    @DisplayName("Tạo đơn hàng thành công - không có variant, đủ hàng")
    void createOrder_success_noVariant() {
      when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
      when(productRepository.save(any(Product.class))).thenReturn(product);
      when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
        Order o = inv.getArgument(0);
        o.setId(ORDER_ID);
        return o;
      });
      doNothing().when(emailService).sendOrderConfirmationEmail(any(), any(), any(), anyDouble());

      OrderResponse result = orderService.createOrder(USER_ID, orderRequest);

      assertThat(result).isNotNull();
      assertThat(result.getUserId()).isEqualTo(USER_ID);
      assertThat(result.getStatus()).isEqualTo(OrderStatus.PENDING);
      assertThat(result.getPaymentStatus()).isEqualTo("PENDING");
      assertThat(result.getItems()).hasSize(1);
      // stock should be reduced: 10 - 2 = 8
      assertThat(product.getStock()).isEqualTo(8);
    }

    @Test
    @DisplayName("Tạo đơn hàng thành công - có variant, giá lấy từ variant")
    void createOrder_success_withVariant() {
      CreateOrderRequest.OrderItemRequest itemReq = new CreateOrderRequest.OrderItemRequest(
          PRODUCT_ID, "Samsung Galaxy S25", "samsung.jpg", "Samsung", "Blue", "256GB", 18000000, 1);
      orderRequest.setItems(List.of(itemReq));

      when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(productWithVariant));
      when(productRepository.save(any(Product.class))).thenReturn(productWithVariant);
      when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
        Order o = inv.getArgument(0);
        o.setId(ORDER_ID);
        return o;
      });
      doNothing().when(emailService).sendOrderConfirmationEmail(any(), any(), any(), anyDouble());

      OrderResponse result = orderService.createOrder(USER_ID, orderRequest);

      assertThat(result.getItems().get(0).getPrice()).isEqualTo(18000000);
      assertThat(result.getItems().get(0).getColor()).isEqualTo("Blue");
      assertThat(result.getItems().get(0).getStorage()).isEqualTo("256GB");
      // variant stock: 5 - 1 = 4
      assertThat(variant.getStock()).isEqualTo(4);
    }

    @Test
    @DisplayName("Tạo đơn hàng - tổng < 500k thì tính phí ship 30k")
    void createOrder_subtotalBelow500k_chargesShippingFee30k() {
      product.setPrice(100000);
      CreateOrderRequest.OrderItemRequest cheapItem = new CreateOrderRequest.OrderItemRequest(
          PRODUCT_ID, "Ốp lưng", "case.jpg", "Generic", null, null, 100000, 2);
      orderRequest.setItems(List.of(cheapItem));

      when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
      when(productRepository.save(any(Product.class))).thenReturn(product);
      when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
        Order o = inv.getArgument(0);
        o.setId(ORDER_ID);
        return o;
      });
      doNothing().when(emailService).sendOrderConfirmationEmail(any(), any(), any(), anyDouble());

      OrderResponse result = orderService.createOrder(USER_ID, orderRequest);

      // subtotal = 100000 * 2 = 200000 < 500000
      assertThat(result.getSubtotal()).isEqualTo(200000);
      assertThat(result.getShippingFee()).isEqualTo(30000);
      assertThat(result.getTotal()).isEqualTo(230000);
    }

    @Test
    @DisplayName("Tạo đơn hàng - tổng >= 500k thì miễn phí ship")
    void createOrder_subtotalAbove500k_freeShipping() {
      when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
      when(productRepository.save(any(Product.class))).thenReturn(product);
      when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
        Order o = inv.getArgument(0);
        o.setId(ORDER_ID);
        return o;
      });
      doNothing().when(emailService).sendOrderConfirmationEmail(any(), any(), any(), anyDouble());

      OrderResponse result = orderService.createOrder(USER_ID, orderRequest);

      // subtotal = 15000000 * 2 = 30000000 >= 500000 → free shipping
      assertThat(result.getShippingFee()).isEqualTo(0);
    }

    @Test
    @DisplayName("Tạo đơn hàng - không đủ hàng trong kho → ném BadRequestException")
    void createOrder_insufficientStock_throwsBadRequestException() {
      product.setStock(1); // only 1 in stock
      // request wants 2
      when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));

      assertThatThrownBy(() -> orderService.createOrder(USER_ID, orderRequest))
          .isInstanceOf(BadRequestException.class)
          .hasMessageContaining("chỉ còn");
    }

    @Test
    @DisplayName("Tạo đơn hàng - sản phẩm không tồn tại → ném ResourceNotFoundException")
    void createOrder_productNotFound_throwsResourceNotFoundException() {
      when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> orderService.createOrder(USER_ID, orderRequest))
          .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Tạo đơn hàng COD - gửi email xác nhận")
    void createOrder_COD_sendsConfirmationEmail() {
      when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
      when(productRepository.save(any(Product.class))).thenReturn(product);
      when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
        Order o = inv.getArgument(0);
        o.setId(ORDER_ID);
        return o;
      });

      orderService.createOrder(USER_ID, orderRequest);

      verify(emailService).sendOrderConfirmationEmail(
          eq("customer@example.com"), eq("Nguyen Van A"), any(), anyDouble());
    }

    @Test
    @DisplayName("Tạo đơn hàng MOMO - KHÔNG gửi email xác nhận")
    void createOrder_MOMO_doesNotSendConfirmationEmail() {
      orderRequest.setPaymentMethod("MOMO");

      when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
      when(productRepository.save(any(Product.class))).thenReturn(product);
      when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
        Order o = inv.getArgument(0);
        o.setId(ORDER_ID);
        return o;
      });

      orderService.createOrder(USER_ID, orderRequest);

      verify(emailService, never()).sendOrderConfirmationEmail(any(), any(), any(), anyDouble());
    }

    @Test
    @DisplayName("Số lượng item < 1 → ném BadRequestException")
    void createOrder_quantityBelowMin_throwsBadRequestException() {
      CreateOrderRequest.OrderItemRequest invalidItem = new CreateOrderRequest.OrderItemRequest(
          PRODUCT_ID, "Samsung", "img.jpg", "Samsung", null, null, 15000000, 0);
      orderRequest.setItems(List.of(invalidItem));

      when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));

      assertThatThrownBy(() -> orderService.createOrder(USER_ID, orderRequest))
          .isInstanceOf(BadRequestException.class)
          .hasMessageContaining("quantity must be between");
    }
  }

  // ───────────────────────────────────────────────────
  // UPDATE STATUS
  // ───────────────────────────────────────────────────
  @Nested
  @DisplayName("updateStatus()")
  class UpdateStatus {

    private Order buildOrder(OrderStatus status, String paymentStatus) {
      OrderItem item = OrderItem.builder()
          .productId(PRODUCT_ID).color("").storage("").quantity(2).price(15000000).build();
      Order order = Order.builder()
          .userId(USER_ID)
          .orderCode("ORD001")
          .email("test@test.com")
          .status(status)
          .paymentStatus(paymentStatus)
          .paymentMethod("COD")
          .items(List.of(item))
          .build();
      order.setId(ORDER_ID);
      return order;
    }

    @Test
    @DisplayName("PENDING → CONFIRMED: chuyển trạng thái hợp lệ")
    void updateStatus_pendingToConfirmed_success() {
      Order order = buildOrder(OrderStatus.PENDING, "PENDING");
      when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
      when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

      OrderResponse result = orderService.updateStatus(ORDER_ID, OrderStatus.CONFIRMED);

      assertThat(result.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
    }

    @Test
    @DisplayName("CONFIRMED → SHIPPING: chuyển trạng thái hợp lệ")
    void updateStatus_confirmedToShipping_success() {
      Order order = buildOrder(OrderStatus.CONFIRMED, "PENDING");
      when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
      when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

      OrderResponse result = orderService.updateStatus(ORDER_ID, OrderStatus.SHIPPING);

      assertThat(result.getStatus()).isEqualTo(OrderStatus.SHIPPING);
    }

    @Test
    @DisplayName("SHIPPING → DELIVERED: chuyển trạng thái hợp lệ")
    void updateStatus_shippingToDelivered_success() {
      Order order = buildOrder(OrderStatus.SHIPPING, "PENDING");
      when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
      when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

      OrderResponse result = orderService.updateStatus(ORDER_ID, OrderStatus.DELIVERED);

      assertThat(result.getStatus()).isEqualTo(OrderStatus.DELIVERED);
    }

    @Test
    @DisplayName("DELIVERED → PENDING: chuyển trạng thái không hợp lệ → ném BadRequestException")
    void updateStatus_deliveredToPending_throwsBadRequestException() {
      Order order = buildOrder(OrderStatus.DELIVERED, "PAID");
      when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));

      assertThatThrownBy(() -> orderService.updateStatus(ORDER_ID, OrderStatus.PENDING))
          .isInstanceOf(BadRequestException.class)
          .hasMessageContaining("Không thể chuyển trạng thái");
    }

    @Test
    @DisplayName("CANCELLED → CONFIRMED: chuyển trạng thái không hợp lệ → ném BadRequestException")
    void updateStatus_cancelledToConfirmed_throwsBadRequestException() {
      Order order = buildOrder(OrderStatus.CANCELLED, "FAILED");
      when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));

      assertThatThrownBy(() -> orderService.updateStatus(ORDER_ID, OrderStatus.CONFIRMED))
          .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("Hủy đơn hàng đã thanh toán (PAID) → ném BadRequestException")
    void updateStatus_cancelPaidOrder_throwsBadRequestException() {
      Order order = buildOrder(OrderStatus.PENDING, "PAID");
      when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));

      assertThatThrownBy(() -> orderService.updateStatus(ORDER_ID, OrderStatus.CANCELLED))
          .isInstanceOf(BadRequestException.class)
          .hasMessageContaining("Đơn hàng đã thanh toán");
    }

    @Test
    @DisplayName("SHIPPING → DELIVERED với COD → tự động chuyển paymentStatus thành PAID")
    void updateStatus_deliveredCOD_setsPaymentStatusPaid() {
      Order order = buildOrder(OrderStatus.SHIPPING, "PENDING");
      order.setPaymentMethod("COD");
      when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
      when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

      OrderResponse result = orderService.updateStatus(ORDER_ID, OrderStatus.DELIVERED);

      assertThat(result.getPaymentStatus()).isEqualTo("PAID");
    }

    @Test
    @DisplayName("Hủy đơn hàng - hoàn lại stock cho sản phẩm")
    void updateStatus_cancel_restoresStock() {
      Order order = buildOrder(OrderStatus.PENDING, "PENDING");
      when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
      when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
      when(productRepository.save(any(Product.class))).thenReturn(product);
      when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

      int stockBefore = product.getStock();
      orderService.updateStatus(ORDER_ID, OrderStatus.CANCELLED);

      // stock should be restored by 2
      assertThat(product.getStock()).isEqualTo(stockBefore + 2);
    }

    @Test
    @DisplayName("Order không tồn tại → ném ResourceNotFoundException")
    void updateStatus_orderNotFound_throwsResourceNotFoundException() {
      when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> orderService.updateStatus(ORDER_ID, OrderStatus.CONFIRMED))
          .isInstanceOf(ResourceNotFoundException.class);
    }
  }

  // ───────────────────────────────────────────────────
  // CANCEL ORDER (by USER)
  // ───────────────────────────────────────────────────
  @Nested
  @DisplayName("cancelOrder()")
  class CancelOrder {

    private Order buildPendingOrder(String paymentStatus) {
      OrderItem item = OrderItem.builder()
          .productId(PRODUCT_ID).color("").storage("").quantity(1).price(15000000).build();
      Order order = Order.builder()
          .userId(USER_ID)
          .orderCode("ORD002")
          .status(OrderStatus.PENDING)
          .paymentStatus(paymentStatus)
          .paymentMethod("COD")
          .items(List.of(item))
          .build();
      order.setId(ORDER_ID);
      return order;
    }

    @Test
    @DisplayName("Hủy đơn hàng PENDING thành công")
    void cancelOrder_pendingOrder_success() {
      Order order = buildPendingOrder("PENDING");
      when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
      when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
      when(productRepository.save(any(Product.class))).thenReturn(product);
      when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

      OrderResponse result = orderService.cancelOrder(ORDER_ID, USER_ID, "Muốn đổi màu khác");

      assertThat(result.getStatus()).isEqualTo(OrderStatus.CANCELLED);
      assertThat(result.getCancelledBy()).isEqualTo("USER");
      assertThat(result.getCancelReason()).isEqualTo("Muốn đổi màu khác");
    }

    @Test
    @DisplayName("Hủy đơn hàng CONFIRMED thành công")
    void cancelOrder_confirmedOrder_success() {
      OrderItem item = OrderItem.builder()
          .productId(PRODUCT_ID).color("").storage("").quantity(1).price(15000000).build();
      Order order = Order.builder()
          .userId(USER_ID)
          .orderCode("ORD002")
          .status(OrderStatus.CONFIRMED)
          .paymentStatus("PENDING")
          .paymentMethod("COD")
          .items(List.of(item))
          .build();
      order.setId(ORDER_ID);

      when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
      when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
      when(productRepository.save(any(Product.class))).thenReturn(product);
      when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

      OrderResponse result = orderService.cancelOrder(ORDER_ID, USER_ID, "Lý do khác");

      assertThat(result.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    @DisplayName("Hủy đơn hàng của người dùng khác → ném BadRequestException")
    void cancelOrder_wrongUser_throwsBadRequestException() {
      Order order = buildPendingOrder("PENDING");
      when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));

      assertThatThrownBy(() -> orderService.cancelOrder(ORDER_ID, "other_user", "reason"))
          .isInstanceOf(BadRequestException.class)
          .hasMessageContaining("không có quyền");
    }

    @Test
    @DisplayName("Hủy đơn hàng đang SHIPPING → ném BadRequestException")
    void cancelOrder_shippingOrder_throwsBadRequestException() {
      OrderItem item = OrderItem.builder()
          .productId(PRODUCT_ID).color("").storage("").quantity(1).price(15000000).build();
      Order order = Order.builder()
          .userId(USER_ID)
          .orderCode("ORD003")
          .status(OrderStatus.SHIPPING)
          .paymentStatus("PENDING")
          .items(List.of(item))
          .build();
      order.setId(ORDER_ID);

      when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));

      assertThatThrownBy(() -> orderService.cancelOrder(ORDER_ID, USER_ID, "reason"))
          .isInstanceOf(BadRequestException.class)
          .hasMessageContaining("Chỉ có thể hủy");
    }

    @Test
    @DisplayName("Hủy đơn hàng đã thanh toán (PAID) → ném BadRequestException")
    void cancelOrder_paidOrder_throwsBadRequestException() {
      Order order = buildPendingOrder("PAID");
      when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));

      assertThatThrownBy(() -> orderService.cancelOrder(ORDER_ID, USER_ID, "reason"))
          .isInstanceOf(BadRequestException.class)
          .hasMessageContaining("hoàn tiền");
    }

    @Test
    @DisplayName("Hủy đơn hàng - hoàn lại stock")
    void cancelOrder_restoresProductStock() {
      Order order = buildPendingOrder("PENDING");
      int stockBefore = product.getStock();

      when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
      when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
      when(productRepository.save(any(Product.class))).thenReturn(product);
      when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

      orderService.cancelOrder(ORDER_ID, USER_ID, "reason");

      // Quantity in order = 1, stock should be restored
      assertThat(product.getStock()).isEqualTo(stockBefore + 1);
    }
  }
}
