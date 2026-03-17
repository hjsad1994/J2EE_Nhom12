package nhom12.example.nhom12.service.impl;

import java.util.ArrayList;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
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
import nhom12.example.nhom12.service.CartService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

  private static final int MAX_QUANTITY = 99;

  private final CartRepository cartRepository;
  private final ProductRepository productRepository;

  @Override
  public CartResponse getCart(String userId) {
    return cartRepository.findByUserId(userId).map(this::toResponse).orElseGet(this::emptyCart);
  }

  @Override
  @Transactional
  public CartResponse addItem(String userId, CartItemRequest request) {
    Product product =
        productRepository
            .findById(request.getProductId())
            .orElseThrow(
                () -> new ResourceNotFoundException("Product", "id", request.getProductId()));
    ProductVariant selectedVariant = findVariant(product, request.getColor(), request.getStorage());

    Cart cart =
        cartRepository
            .findByUserId(userId)
            .orElseGet(() -> Cart.builder().userId(userId).items(new ArrayList<>()).build());

    Optional<CartItem> existing =
        cart.getItems().stream()
            .filter(
                i ->
                    matchesItem(
                        i,
                        request.getProductId(),
                        normalizeOption(request.getColor()),
                        normalizeOption(request.getStorage())))
            .findFirst();

    if (existing.isPresent()) {
      int newQty = Math.min(existing.get().getQuantity() + request.getQuantity(), MAX_QUANTITY);
      existing.get().setQuantity(newQty);
    } else {
      cart.getItems()
          .add(
              CartItem.builder()
                  .productId(product.getId())
                  .productName(product.getName())
                  .productImage(resolveCartImage(product, selectedVariant))
                  .brand(product.getBrand())
                  .color(normalizeOption(request.getColor()))
                  .storage(normalizeOption(request.getStorage()))
                  .price(selectedVariant != null ? selectedVariant.getPrice() : product.getPrice())
                  .quantity(request.getQuantity())
                  .build());
    }

    return toResponse(cartRepository.save(cart));
  }

  @Override
  @Transactional
  public CartResponse updateItemQuantity(
      String userId, String productId, String color, String storage, int quantity) {
    Cart cart =
        cartRepository
            .findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Cart", "userId", userId));

    if (quantity <= 0) {
      cart.getItems().removeIf(i -> matchesItem(i, productId, color, storage));
    } else {
      cart.getItems().stream()
          .filter(i -> matchesItem(i, productId, color, storage))
          .findFirst()
          .ifPresent(i -> i.setQuantity(Math.min(quantity, MAX_QUANTITY)));
    }

    return toResponse(cartRepository.save(cart));
  }

  @Override
  @Transactional
  public CartResponse removeItem(String userId, String productId, String color, String storage) {
    Cart cart =
        cartRepository
            .findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Cart", "userId", userId));

    cart.getItems().removeIf(i -> matchesItem(i, productId, color, storage));
    return toResponse(cartRepository.save(cart));
  }

  /**
   * Merge guest cart (localStorage items sent on login) with the server cart. Strategy: - Items
   * only in local → add to server - Items in both → take the higher quantity (cap at 99) - Items
   * only on server → keep unchanged
   */
  @Override
  @Transactional
  public CartResponse syncCart(String userId, CartSyncRequest request) {
    if (request.getItems() == null || request.getItems().isEmpty()) {
      return getCart(userId);
    }

    Cart cart =
        cartRepository
            .findByUserId(userId)
            .orElseGet(() -> Cart.builder().userId(userId).items(new ArrayList<>()).build());

    for (CartItemRequest localItem : request.getItems()) {
      Product product = productRepository.findById(localItem.getProductId()).orElse(null);
      if (product == null) continue; // skip products that no longer exist

      Optional<CartItem> serverItem =
          cart.getItems().stream()
              .filter(
                  i ->
                      matchesItem(
                          i,
                          localItem.getProductId(),
                          normalizeOption(localItem.getColor()),
                          normalizeOption(localItem.getStorage())))
              .findFirst();

      if (serverItem.isPresent()) {
        // Take the higher quantity of local vs server
        int merged =
            Math.min(
                Math.max(serverItem.get().getQuantity(), localItem.getQuantity()), MAX_QUANTITY);
        serverItem.get().setQuantity(merged);
      } else {
        ProductVariant selectedVariant =
            findVariant(product, localItem.getColor(), localItem.getStorage());
        // Add new item from local cart with fresh product data
        cart.getItems()
            .add(
                CartItem.builder()
                    .productId(product.getId())
                    .productName(product.getName())
                    .productImage(resolveCartImage(product, selectedVariant))
                    .brand(product.getBrand())
                    .color(normalizeOption(localItem.getColor()))
                    .storage(normalizeOption(localItem.getStorage()))
                    .price(
                        selectedVariant != null ? selectedVariant.getPrice() : product.getPrice())
                    .quantity(Math.min(localItem.getQuantity(), MAX_QUANTITY))
                    .build());
      }
    }

    return toResponse(cartRepository.save(cart));
  }

  @Override
  @Transactional
  public void clearCart(String userId) {
    cartRepository
        .findByUserId(userId)
        .ifPresent(
            cart -> {
              cart.getItems().clear();
              cartRepository.save(cart);
            });
  }

  private CartResponse toResponse(Cart cart) {
    return CartResponse.builder()
        .id(cart.getId())
        .userId(cart.getUserId())
        .items(cart.getItems())
        .updatedAt(cart.getUpdatedAt())
        .build();
  }

  private CartResponse emptyCart() {
    return CartResponse.builder().items(new ArrayList<>()).build();
  }

  private boolean matchesItem(CartItem item, String productId, String color, String storage) {
    return item.getProductId().equals(productId)
        && normalizeOption(item.getColor()).equals(normalizeOption(color))
        && normalizeOption(item.getStorage()).equals(normalizeOption(storage));
  }

  private String normalizeOption(String value) {
    return value == null ? "" : value.trim();
  }

  private ProductVariant findVariant(Product product, String color, String storage) {
    String normalizedColor = normalizeOption(color);
    String normalizedStorage = normalizeOption(storage);

    if (normalizedColor.isBlank() && normalizedStorage.isBlank()) {
      return null;
    }

    return product.getVariants() == null
        ? null
        : product.getVariants().stream()
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

  private String resolveCartImage(Product product, ProductVariant variant) {
    if (variant != null && variant.getImage() != null && !variant.getImage().isBlank()) {
      return variant.getImage();
    }
    return product.getImage();
  }
}
