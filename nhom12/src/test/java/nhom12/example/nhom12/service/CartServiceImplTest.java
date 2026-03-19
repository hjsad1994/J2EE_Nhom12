package nhom12.example.nhom12.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import nhom12.example.nhom12.dto.request.CartItemRequest;
import nhom12.example.nhom12.dto.request.CartSyncRequest;
import nhom12.example.nhom12.dto.response.CartResponse;
import nhom12.example.nhom12.exception.ResourceNotFoundException;
import nhom12.example.nhom12.model.Cart;
import nhom12.example.nhom12.model.CartItem;
import nhom12.example.nhom12.model.Product;
import nhom12.example.nhom12.model.ProductVariant;
import nhom12.example.nhom12.repository.CartRepository;
import nhom12.example.nhom12.repository.ProductRepository;
import nhom12.example.nhom12.service.impl.CartServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("CartServiceImpl - Unit Tests")
class CartServiceImplTest {

  @Mock private CartRepository cartRepository;
  @Mock private ProductRepository productRepository;

  @InjectMocks private CartServiceImpl cartService;

  private static final String USER_ID = "user001";
  private static final String PRODUCT_ID = "prod001";

  private Product baseProduct;
  private Product productWithVariants;
  private ProductVariant variant;

  @BeforeEach
  void setUp() {
    baseProduct = new Product();
    baseProduct.setId(PRODUCT_ID);
    baseProduct.setName("iPhone 15");
    baseProduct.setBrand("Apple");
    baseProduct.setImage("iphone.jpg");
    baseProduct.setPrice(20000000);
    baseProduct.setStock(10);

    variant = ProductVariant.builder()
        .color("Black")
        .storage("128GB")
        .price(22000000)
        .stock(5)
        .image("iphone-black.jpg")
        .build();

    productWithVariants = new Product();
    productWithVariants.setId(PRODUCT_ID);
    productWithVariants.setName("iPhone 15");
    productWithVariants.setBrand("Apple");
    productWithVariants.setImage("iphone.jpg");
    productWithVariants.setPrice(22000000);
    productWithVariants.setVariants(new ArrayList<>(List.of(variant)));
  }

  // ───────────────────────────────────────────────────
  // GET CART
  // ───────────────────────────────────────────────────
  @Nested
  @DisplayName("getCart()")
  class GetCart {

    @Test
    @DisplayName("Lấy giỏ hàng thành công khi giỏ hàng tồn tại")
    void getCart_existingCart_returnsCartResponse() {
      Cart cart = Cart.builder().userId(USER_ID).items(new ArrayList<>()).build();
      cart.setId("cart001");

      when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));

      CartResponse result = cartService.getCart(USER_ID);

      assertThat(result).isNotNull();
      assertThat(result.getUserId()).isEqualTo(USER_ID);
      assertThat(result.getItems()).isEmpty();
    }

    @Test
    @DisplayName("Lấy giỏ hàng rỗng khi chưa có giỏ hàng")
    void getCart_noCart_returnsEmptyCart() {
      when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

      CartResponse result = cartService.getCart(USER_ID);

      assertThat(result).isNotNull();
      assertThat(result.getItems()).isEmpty();
    }
  }

  // ───────────────────────────────────────────────────
  // ADD ITEM
  // ───────────────────────────────────────────────────
  @Nested
  @DisplayName("addItem()")
  class AddItem {

    @Test
    @DisplayName("Thêm sản phẩm mới không có variant vào giỏ hàng trống")
    void addItem_newProduct_noVariant_addsToCart() {
      CartItemRequest request = new CartItemRequest(PRODUCT_ID, null, null, 2);
      Cart savedCart = Cart.builder().userId(USER_ID).items(new ArrayList<>()).build();

      when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(baseProduct));
      when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());
      when(cartRepository.save(any(Cart.class))).thenAnswer(inv -> {
        Cart c = inv.getArgument(0);
        c.setId("cart001");
        return c;
      });

      CartResponse result = cartService.addItem(USER_ID, request);

      assertThat(result.getItems()).hasSize(1);
      CartItem item = result.getItems().get(0);
      assertThat(item.getProductId()).isEqualTo(PRODUCT_ID);
      assertThat(item.getQuantity()).isEqualTo(2);
      assertThat(item.getPrice()).isEqualTo(20000000);
    }

    @Test
    @DisplayName("Thêm sản phẩm có variant - giá lấy từ variant")
    void addItem_withVariant_usesVariantPrice() {
      CartItemRequest request = new CartItemRequest(PRODUCT_ID, "Black", "128GB", 1);

      when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(productWithVariants));
      when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());
      when(cartRepository.save(any(Cart.class))).thenAnswer(inv -> inv.getArgument(0));

      CartResponse result = cartService.addItem(USER_ID, request);

      assertThat(result.getItems()).hasSize(1);
      assertThat(result.getItems().get(0).getPrice()).isEqualTo(22000000);
      assertThat(result.getItems().get(0).getColor()).isEqualTo("Black");
      assertThat(result.getItems().get(0).getStorage()).isEqualTo("128GB");
    }

    @Test
    @DisplayName("Thêm sản phẩm đã có trong giỏ hàng - cộng dồn số lượng")
    void addItem_existingItem_incrementsQuantity() {
      CartItem existingItem = CartItem.builder()
          .productId(PRODUCT_ID)
          .color("")
          .storage("")
          .quantity(3)
          .price(20000000)
          .build();
      Cart cart = Cart.builder().userId(USER_ID).items(new ArrayList<>(List.of(existingItem))).build();

      CartItemRequest request = new CartItemRequest(PRODUCT_ID, null, null, 5);

      when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(baseProduct));
      when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));
      when(cartRepository.save(any(Cart.class))).thenAnswer(inv -> inv.getArgument(0));

      CartResponse result = cartService.addItem(USER_ID, request);

      assertThat(result.getItems()).hasSize(1);
      assertThat(result.getItems().get(0).getQuantity()).isEqualTo(8); // 3 + 5
    }

    @Test
    @DisplayName("Cộng dồn số lượng vượt quá 99 - giới hạn tại 99")
    void addItem_quantityExceedsMax_capsAt99() {
      CartItem existingItem = CartItem.builder()
          .productId(PRODUCT_ID)
          .color("")
          .storage("")
          .quantity(95)
          .price(20000000)
          .build();
      Cart cart = Cart.builder().userId(USER_ID).items(new ArrayList<>(List.of(existingItem))).build();

      CartItemRequest request = new CartItemRequest(PRODUCT_ID, null, null, 10);

      when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(baseProduct));
      when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));
      when(cartRepository.save(any(Cart.class))).thenAnswer(inv -> inv.getArgument(0));

      CartResponse result = cartService.addItem(USER_ID, request);

      assertThat(result.getItems().get(0).getQuantity()).isEqualTo(99);
    }

    @Test
    @DisplayName("Sản phẩm không tồn tại - ném ResourceNotFoundException")
    void addItem_productNotFound_throwsResourceNotFoundException() {
      CartItemRequest request = new CartItemRequest("nonexistent", null, null, 1);
      when(productRepository.findById("nonexistent")).thenReturn(Optional.empty());

      assertThatThrownBy(() -> cartService.addItem(USER_ID, request))
          .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Thêm variant không tồn tại - ném ResourceNotFoundException")
    void addItem_variantNotFound_throwsResourceNotFoundException() {
      CartItemRequest request = new CartItemRequest(PRODUCT_ID, "Gold", "512GB", 1);
      when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(productWithVariants));
      // findVariant throws before cartRepository is called, so no cart stub needed

      assertThatThrownBy(() -> cartService.addItem(USER_ID, request))
          .isInstanceOf(ResourceNotFoundException.class);
    }
  }

  // ───────────────────────────────────────────────────
  // UPDATE ITEM QUANTITY
  // ───────────────────────────────────────────────────
  @Nested
  @DisplayName("updateItemQuantity()")
  class UpdateItemQuantity {

    @Test
    @DisplayName("Cập nhật số lượng hợp lệ")
    void updateItemQuantity_validQuantity_updatesCorrectly() {
      CartItem item = CartItem.builder()
          .productId(PRODUCT_ID).color("").storage("").quantity(3).build();
      Cart cart = Cart.builder().userId(USER_ID).items(new ArrayList<>(List.of(item))).build();

      when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));
      when(cartRepository.save(any(Cart.class))).thenAnswer(inv -> inv.getArgument(0));

      CartResponse result = cartService.updateItemQuantity(USER_ID, PRODUCT_ID, "", "", 7);

      assertThat(result.getItems().get(0).getQuantity()).isEqualTo(7);
    }

    @Test
    @DisplayName("Số lượng = 0 - xóa item khỏi giỏ hàng")
    void updateItemQuantity_zeroQuantity_removesItem() {
      CartItem item = CartItem.builder()
          .productId(PRODUCT_ID).color("").storage("").quantity(3).build();
      Cart cart = Cart.builder().userId(USER_ID).items(new ArrayList<>(List.of(item))).build();

      when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));
      when(cartRepository.save(any(Cart.class))).thenAnswer(inv -> inv.getArgument(0));

      CartResponse result = cartService.updateItemQuantity(USER_ID, PRODUCT_ID, "", "", 0);

      assertThat(result.getItems()).isEmpty();
    }

    @Test
    @DisplayName("Số lượng vượt MAX - giới hạn tại 99")
    void updateItemQuantity_exceedsMax_capsAt99() {
      CartItem item = CartItem.builder()
          .productId(PRODUCT_ID).color("").storage("").quantity(5).build();
      Cart cart = Cart.builder().userId(USER_ID).items(new ArrayList<>(List.of(item))).build();

      when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));
      when(cartRepository.save(any(Cart.class))).thenAnswer(inv -> inv.getArgument(0));

      CartResponse result = cartService.updateItemQuantity(USER_ID, PRODUCT_ID, "", "", 200);

      assertThat(result.getItems().get(0).getQuantity()).isEqualTo(99);
    }

    @Test
    @DisplayName("Không tìm thấy giỏ hàng - ném ResourceNotFoundException")
    void updateItemQuantity_cartNotFound_throwsResourceNotFoundException() {
      when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> cartService.updateItemQuantity(USER_ID, PRODUCT_ID, "", "", 3))
          .isInstanceOf(ResourceNotFoundException.class);
    }
  }

  // ───────────────────────────────────────────────────
  // REMOVE ITEM
  // ───────────────────────────────────────────────────
  @Nested
  @DisplayName("removeItem()")
  class RemoveItem {

    @Test
    @DisplayName("Xóa item thành công")
    void removeItem_success_removesFromCart() {
      CartItem item = CartItem.builder()
          .productId(PRODUCT_ID).color("").storage("").quantity(2).build();
      Cart cart = Cart.builder().userId(USER_ID).items(new ArrayList<>(List.of(item))).build();

      when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));
      when(cartRepository.save(any(Cart.class))).thenAnswer(inv -> inv.getArgument(0));

      CartResponse result = cartService.removeItem(USER_ID, PRODUCT_ID, "", "");

      assertThat(result.getItems()).isEmpty();
    }

    @Test
    @DisplayName("Giỏ hàng không tồn tại - ném ResourceNotFoundException")
    void removeItem_cartNotFound_throwsResourceNotFoundException() {
      when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> cartService.removeItem(USER_ID, PRODUCT_ID, "", ""))
          .isInstanceOf(ResourceNotFoundException.class);
    }
  }

  // ───────────────────────────────────────────────────
  // CLEAR CART
  // ───────────────────────────────────────────────────
  @Nested
  @DisplayName("clearCart()")
  class ClearCart {

    @Test
    @DisplayName("Xóa toàn bộ giỏ hàng thành công")
    void clearCart_success_clearsAllItems() {
      CartItem item = CartItem.builder()
          .productId(PRODUCT_ID).color("").storage("").quantity(2).build();
      Cart cart = Cart.builder().userId(USER_ID).items(new ArrayList<>(List.of(item))).build();

      when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));
      when(cartRepository.save(any(Cart.class))).thenAnswer(inv -> inv.getArgument(0));

      cartService.clearCart(USER_ID);

      ArgumentCaptor<Cart> cartCaptor = ArgumentCaptor.forClass(Cart.class);
      verify(cartRepository).save(cartCaptor.capture());
      assertThat(cartCaptor.getValue().getItems()).isEmpty();
    }

    @Test
    @DisplayName("Giỏ hàng không tồn tại - không làm gì")
    void clearCart_noCart_doesNothing() {
      when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

      cartService.clearCart(USER_ID);

      verify(cartRepository, never()).save(any());
    }
  }

  // ───────────────────────────────────────────────────
  // SYNC CART
  // ───────────────────────────────────────────────────
  @Nested
  @DisplayName("syncCart()")
  class SyncCart {

    @Test
    @DisplayName("Sync với danh sách rỗng - trả về giỏ hàng hiện tại")
    void syncCart_emptyLocalItems_returnsCurrentCart() {
      Cart cart = Cart.builder().userId(USER_ID).items(new ArrayList<>()).build();
      CartSyncRequest request = new CartSyncRequest();
      request.setItems(List.of());

      when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));

      CartResponse result = cartService.syncCart(USER_ID, request);

      assertThat(result.getItems()).isEmpty();
      verify(cartRepository, never()).save(any());
    }

    @Test
    @DisplayName("Sync - item đã có trong server, lấy số lượng lớn hơn")
    void syncCart_existingItem_takesHigherQuantity() {
      CartItem serverItem = CartItem.builder()
          .productId(PRODUCT_ID).color("").storage("").quantity(3).price(20000000).build();
      Cart serverCart = Cart.builder().userId(USER_ID).items(new ArrayList<>(List.of(serverItem))).build();

      CartItemRequest localItem = new CartItemRequest(PRODUCT_ID, null, null, 5);
      CartSyncRequest request = new CartSyncRequest();
      request.setItems(List.of(localItem));

      when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(baseProduct));
      when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(serverCart));
      when(cartRepository.save(any(Cart.class))).thenAnswer(inv -> inv.getArgument(0));

      CartResponse result = cartService.syncCart(USER_ID, request);

      // Local = 5, Server = 3 → merged = 5
      assertThat(result.getItems().get(0).getQuantity()).isEqualTo(5);
    }

    @Test
    @DisplayName("Sync - item chỉ có local, thêm vào server cart")
    void syncCart_localOnlyItem_addsToServerCart() {
      Cart serverCart = Cart.builder().userId(USER_ID).items(new ArrayList<>()).build();

      CartItemRequest localItem = new CartItemRequest(PRODUCT_ID, null, null, 2);
      CartSyncRequest request = new CartSyncRequest();
      request.setItems(List.of(localItem));

      when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(baseProduct));
      when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(serverCart));
      when(cartRepository.save(any(Cart.class))).thenAnswer(inv -> inv.getArgument(0));

      CartResponse result = cartService.syncCart(USER_ID, request);

      assertThat(result.getItems()).hasSize(1);
      assertThat(result.getItems().get(0).getQuantity()).isEqualTo(2);
    }

    @Test
    @DisplayName("Sync - sản phẩm không tồn tại bị bỏ qua")
    void syncCart_productNotFound_itemIsSkipped() {
      Cart serverCart = Cart.builder().userId(USER_ID).items(new ArrayList<>()).build();

      CartItemRequest localItem = new CartItemRequest("deleted_product", null, null, 1);
      CartSyncRequest request = new CartSyncRequest();
      request.setItems(List.of(localItem));

      when(productRepository.findById("deleted_product")).thenReturn(Optional.empty());
      when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(serverCart));
      when(cartRepository.save(any(Cart.class))).thenAnswer(inv -> inv.getArgument(0));

      CartResponse result = cartService.syncCart(USER_ID, request);

      assertThat(result.getItems()).isEmpty();
    }
  }
}
