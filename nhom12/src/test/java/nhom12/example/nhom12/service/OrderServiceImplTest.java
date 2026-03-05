package nhom12.example.nhom12.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import nhom12.example.nhom12.dto.request.CreateOrderRequest;
import nhom12.example.nhom12.exception.BadRequestException;
import nhom12.example.nhom12.exception.ResourceNotFoundException;
import nhom12.example.nhom12.model.Order;
import nhom12.example.nhom12.model.OrderItem;
import nhom12.example.nhom12.model.Product;
import nhom12.example.nhom12.repository.OrderRepository;
import nhom12.example.nhom12.repository.ProductRepository;
import nhom12.example.nhom12.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

  @Mock private OrderRepository orderRepository;
  @Mock private ProductRepository productRepository;

  private OrderServiceImpl orderService;

  @BeforeEach
  void setUp() {
    orderService = new OrderServiceImpl(orderRepository, productRepository);
  }

  @Test
  void createOrder_usesServerSidePricingAndTotals() {
    Product product =
        Product.builder()
            .id("p1")
            .name("Server Product")
            .brand("BrandX")
            .image("image.png")
            .price(100_000)
            .build();
    when(productRepository.findAllById(List.of("p1"))).thenReturn(List.of(product));
    when(orderRepository.save(any(Order.class)))
        .thenAnswer(invocation -> invocation.getArgument(0, Order.class));

    CreateOrderRequest request = new CreateOrderRequest();
    request.setEmail("user@example.com");
    request.setCustomerName("User");
    request.setPhone("123");
    request.setAddress("Addr");
    request.setCity("City");
    request.setDistrict("District");
    request.setWard("Ward");
    request.setPaymentMethod("COD");
    request.setItems(
        List.of(
            new CreateOrderRequest.OrderItemRequest(
                "p1", "hacked", "fake.png", "FakeBrand", 1, 2)));

    orderService.createOrder("u1", request);

    ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
    verify(orderRepository).save(orderCaptor.capture());
    Order saved = orderCaptor.getValue();
    OrderItem savedItem = saved.getItems().get(0);

    assertThat(savedItem.getPrice()).isEqualTo(100_000);
    assertThat(savedItem.getProductName()).isEqualTo("Server Product");
    assertThat(saved.getSubtotal()).isEqualTo(200_000);
    assertThat(saved.getShippingFee()).isEqualTo(30_000);
    assertThat(saved.getTotal()).isEqualTo(230_000);
  }

  @Test
  void createOrder_rejectsNonPositiveQuantity() {
    Product product = Product.builder().id("p1").name("Server Product").price(100_000).build();
    when(productRepository.findAllById(List.of("p1"))).thenReturn(List.of(product));

    CreateOrderRequest request = new CreateOrderRequest();
    request.setItems(List.of(new CreateOrderRequest.OrderItemRequest("p1", null, null, null, 0, 0)));

    assertThatThrownBy(() -> orderService.createOrder("u1", request))
        .isInstanceOf(BadRequestException.class);
  }

  @Test
  void createOrder_throwsWhenProductMissing() {
    when(productRepository.findAllById(List.of("missing"))).thenReturn(List.of());

    CreateOrderRequest request = new CreateOrderRequest();
    request.setItems(
        List.of(new CreateOrderRequest.OrderItemRequest("missing", null, null, null, 0, 1)));

    assertThatThrownBy(() -> orderService.createOrder("u1", request))
        .isInstanceOf(ResourceNotFoundException.class);
    verify(productRepository).findAllById(eq(List.of("missing")));
  }
}
