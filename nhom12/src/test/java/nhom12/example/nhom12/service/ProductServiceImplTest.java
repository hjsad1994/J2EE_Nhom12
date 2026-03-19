package nhom12.example.nhom12.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import nhom12.example.nhom12.dto.request.CreateProductRequest;
import nhom12.example.nhom12.dto.response.ProductResponse;
import nhom12.example.nhom12.exception.BadRequestException;
import nhom12.example.nhom12.exception.ResourceNotFoundException;
import nhom12.example.nhom12.mapper.ProductMapper;
import nhom12.example.nhom12.model.Category;
import nhom12.example.nhom12.model.Order;
import nhom12.example.nhom12.model.Product;
import nhom12.example.nhom12.model.ProductVariant;
import nhom12.example.nhom12.model.enums.OrderStatus;
import nhom12.example.nhom12.repository.CategoryRepository;
import nhom12.example.nhom12.repository.OrderRepository;
import nhom12.example.nhom12.repository.ProductRepository;
import nhom12.example.nhom12.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductServiceImpl - Unit Tests")
class ProductServiceImplTest {

  @Mock private ProductRepository productRepository;
  @Mock private CategoryRepository categoryRepository;
  @Mock private OrderRepository orderRepository;
  @Mock private ProductMapper productMapper;

  @InjectMocks private ProductServiceImpl productService;

  private static final String PRODUCT_ID = "prod001";
  private static final String CATEGORY_ID = "cat001";

  private Product sampleProduct;
  private CreateProductRequest createRequest;
  private Category category;

  @BeforeEach
  void setUp() {
    sampleProduct = new Product();
    sampleProduct.setId(PRODUCT_ID);
    sampleProduct.setName("iPhone 16");
    sampleProduct.setBrand("Apple");
    sampleProduct.setCategoryId(CATEGORY_ID);
    sampleProduct.setPrice(25000000);
    sampleProduct.setImage("iphone16.jpg");
    sampleProduct.setStock(20);

    category = new Category();
    category.setId(CATEGORY_ID);
    category.setName("Điện thoại");

    createRequest = new CreateProductRequest(
        "iPhone 16", "Apple", CATEGORY_ID, 25000000.0, 28000000.0,
        "iphone16.jpg", 0.0, "NEW", null, 20, null);
  }

  // ───────────────────────────────────────────────────
  // CREATE PRODUCT
  // ───────────────────────────────────────────────────
  @Nested
  @DisplayName("createProduct()")
  class CreateProduct {

    @Test
    @DisplayName("Tạo sản phẩm không có variant thành công")
    void createProduct_noVariants_success() {
      when(productMapper.toEntity(createRequest)).thenReturn(sampleProduct);
      when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);
      when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(category));
      when(productMapper.toResponse(sampleProduct, category)).thenReturn(
          ProductResponse.builder()
              .id(PRODUCT_ID).name("iPhone 16").price(25000000).stock(20).build());

      ProductResponse result = productService.createProduct(createRequest);

      assertThat(result).isNotNull();
      assertThat(result.getName()).isEqualTo("iPhone 16");
      assertThat(result.getPrice()).isEqualTo(25000000);
      verify(productRepository).save(any(Product.class));
    }

    @Test
    @DisplayName("Tạo sản phẩm có variant - giá và stock đồng bộ từ variants")
    void createProduct_withVariants_syncsPriceAndStockFromPrimaryVariant() {
      ProductVariant v1 = ProductVariant.builder().color("Black").storage("128GB").price(22000000).stock(10).build();
      ProductVariant v2 = ProductVariant.builder().color("White").storage("256GB").price(25000000).stock(8).build();

      Product productWithVariants = new Product();
      productWithVariants.setId(PRODUCT_ID);
      productWithVariants.setName("iPhone 16");
      productWithVariants.setBrand("Apple");
      productWithVariants.setVariants(new ArrayList<>(List.of(v1, v2)));

      when(productMapper.toEntity(createRequest)).thenReturn(productWithVariants);
      when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));
      // productWithVariants has no categoryId set, so categoryRepository won't be called
      when(productMapper.toResponse(any(Product.class), any())).thenReturn(
          ProductResponse.builder()
              .id(PRODUCT_ID).name("iPhone 16").price(22000000).stock(18).build());

      ProductResponse result = productService.createProduct(createRequest);

      // After sync: price = v1.price = 22000000, stock = 10 + 8 = 18
      assertThat(productWithVariants.getPrice()).isEqualTo(22000000);
      assertThat(productWithVariants.getStock()).isEqualTo(18);
    }
  }

  // ───────────────────────────────────────────────────
  // GET PRODUCT BY ID
  // ───────────────────────────────────────────────────
  @Nested
  @DisplayName("getProductById()")
  class GetProductById {

    @Test
    @DisplayName("Lấy sản phẩm thành công")
    void getProductById_exists_returnsProduct() {
      when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(sampleProduct));
      when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(category));
      when(productMapper.toResponse(sampleProduct, category)).thenReturn(
          ProductResponse.builder().id(PRODUCT_ID).name("iPhone 16").build());

      ProductResponse result = productService.getProductById(PRODUCT_ID);

      assertThat(result).isNotNull();
      assertThat(result.getId()).isEqualTo(PRODUCT_ID);
    }

    @Test
    @DisplayName("Sản phẩm không tồn tại → ném ResourceNotFoundException")
    void getProductById_notFound_throwsResourceNotFoundException() {
      when(productRepository.findById("invalid_id")).thenReturn(Optional.empty());

      assertThatThrownBy(() -> productService.getProductById("invalid_id"))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessageContaining("Product");
    }
  }

  // ───────────────────────────────────────────────────
  // UPDATE PRODUCT
  // ───────────────────────────────────────────────────
  @Nested
  @DisplayName("updateProduct()")
  class UpdateProduct {

    @Test
    @DisplayName("Cập nhật sản phẩm thành công")
    void updateProduct_success_updatesAllFields() {
      CreateProductRequest updateReq = new CreateProductRequest(
          "iPhone 16 Pro", "Apple", CATEGORY_ID, 30000000.0, 33000000.0,
          "iphone16pro.jpg", 4.8, "HOT", "specs...", 15, null);

      when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(sampleProduct));
      when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);
      when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(category));
      when(productMapper.toResponse(any(Product.class), any())).thenReturn(
          ProductResponse.builder().id(PRODUCT_ID).name("iPhone 16 Pro").price(30000000).build());

      ProductResponse result = productService.updateProduct(PRODUCT_ID, updateReq);

      assertThat(sampleProduct.getName()).isEqualTo("iPhone 16 Pro");
      assertThat(sampleProduct.getPrice()).isEqualTo(30000000);
    }

    @Test
    @DisplayName("Cập nhật sản phẩm không tồn tại → ném ResourceNotFoundException")
    void updateProduct_notFound_throwsResourceNotFoundException() {
      when(productRepository.findById("bad_id")).thenReturn(Optional.empty());

      assertThatThrownBy(() -> productService.updateProduct("bad_id", createRequest))
          .isInstanceOf(ResourceNotFoundException.class);
    }
  }

  // ───────────────────────────────────────────────────
  // DELETE PRODUCT
  // ───────────────────────────────────────────────────
  @Nested
  @DisplayName("deleteProduct()")
  class DeleteProduct {

    @Test
    @DisplayName("Xóa sản phẩm thành công - không có active orders")
    void deleteProduct_noActiveOrders_deletesSuccessfully() {
      when(productRepository.existsById(PRODUCT_ID)).thenReturn(true);
      when(orderRepository.findByProductIdAndStatusIn(eq(PRODUCT_ID), any())).thenReturn(List.of());

      productService.deleteProduct(PRODUCT_ID);

      verify(productRepository).deleteById(PRODUCT_ID);
    }

    @Test
    @DisplayName("Xóa sản phẩm đang có active orders → ném BadRequestException")
    void deleteProduct_hasActiveOrders_throwsBadRequestException() {
      Order activeOrder = Order.builder().status(OrderStatus.PENDING).build();
      activeOrder.setId("order001");

      when(productRepository.existsById(PRODUCT_ID)).thenReturn(true);
      when(orderRepository.findByProductIdAndStatusIn(eq(PRODUCT_ID), any()))
          .thenReturn(List.of(activeOrder));

      assertThatThrownBy(() -> productService.deleteProduct(PRODUCT_ID))
          .isInstanceOf(BadRequestException.class)
          .hasMessageContaining("Không thể xóa");

      verify(productRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Xóa sản phẩm không tồn tại → ném ResourceNotFoundException")
    void deleteProduct_notFound_throwsResourceNotFoundException() {
      when(productRepository.existsById("nonexistent")).thenReturn(false);

      assertThatThrownBy(() -> productService.deleteProduct("nonexistent"))
          .isInstanceOf(ResourceNotFoundException.class);
    }
  }
}
