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

    Cart cart =
        cartRepository
            .findByUserId(userId)
            .orElseGet(() -> Cart.builder().userId(userId).items(new ArrayList<>()).build());

    Optional<CartItem> existing =
        cart.getItems().stream()
            .filter(i -> i.getProductId().equals(request.getProductId()))
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
                  .productImage(product.getImage())
                  .brand(product.getBrand())
                  .price(product.getPrice())
                  .quantity(request.getQuantity())
                  .build());
    }

    return toResponse(cartRepository.save(cart));
  }

  @Override
  @Transactional
  public CartResponse updateItemQuantity(String userId, String productId, int quantity) {
    Cart cart =
        cartRepository
            .findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Cart", "userId", userId));

    if (quantity <= 0) {
      cart.getItems().removeIf(i -> i.getProductId().equals(productId));
    } else {
      cart.getItems().stream()
          .filter(i -> i.getProductId().equals(productId))
          .findFirst()
          .ifPresent(i -> i.setQuantity(Math.min(quantity, MAX_QUANTITY)));
    }

    return toResponse(cartRepository.save(cart));
  }

  @Override
  @Transactional
  public CartResponse removeItem(String userId, String productId) {
    Cart cart =
        cartRepository
            .findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Cart", "userId", userId));

    cart.getItems().removeIf(i -> i.getProductId().equals(productId));
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
              .filter(i -> i.getProductId().equals(localItem.getProductId()))
              .findFirst();

      if (serverItem.isPresent()) {
        // Take the higher quantity of local vs server
        int merged =
            Math.min(
                Math.max(serverItem.get().getQuantity(), localItem.getQuantity()), MAX_QUANTITY);
        serverItem.get().setQuantity(merged);
      } else {
        // Add new item from local cart with fresh product data
        cart.getItems()
            .add(
                CartItem.builder()
                    .productId(product.getId())
                    .productName(product.getName())
                    .productImage(product.getImage())
                    .brand(product.getBrand())
                    .price(product.getPrice())
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
}
