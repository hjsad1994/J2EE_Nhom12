package nhom12.example.nhom12.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nhom12.example.nhom12.dto.request.CartItemRequest;
import nhom12.example.nhom12.dto.request.CartSyncRequest;
import nhom12.example.nhom12.dto.response.ApiResponse;
import nhom12.example.nhom12.dto.response.CartResponse;
import nhom12.example.nhom12.exception.ResourceNotFoundException;
import nhom12.example.nhom12.model.User;
import nhom12.example.nhom12.repository.UserRepository;
import nhom12.example.nhom12.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

  private final CartService cartService;
  private final UserRepository userRepository;

  @GetMapping
  public ResponseEntity<ApiResponse<CartResponse>> getCart(
      @AuthenticationPrincipal UserDetails principal) {
    return ResponseEntity.ok(
        ApiResponse.success(cartService.getCart(resolveUserId(principal)), "Cart retrieved"));
  }

  @PostMapping("/items")
  public ResponseEntity<ApiResponse<CartResponse>> addItem(
      @AuthenticationPrincipal UserDetails principal, @Valid @RequestBody CartItemRequest request) {
    return ResponseEntity.ok(
        ApiResponse.success(
            cartService.addItem(resolveUserId(principal), request), "Item added to cart"));
  }

  @PutMapping("/items/{productId}")
  public ResponseEntity<ApiResponse<CartResponse>> updateQuantity(
      @AuthenticationPrincipal UserDetails principal,
      @PathVariable String productId,
      @RequestParam(required = false) String color,
      @RequestParam(required = false) String storage,
      @RequestParam int quantity) {
    return ResponseEntity.ok(
        ApiResponse.success(
            cartService.updateItemQuantity(
                resolveUserId(principal), productId, color, storage, quantity),
            "Quantity updated"));
  }

  @DeleteMapping("/items/{productId}")
  public ResponseEntity<ApiResponse<CartResponse>> removeItem(
      @AuthenticationPrincipal UserDetails principal,
      @PathVariable String productId,
      @RequestParam(required = false) String color,
      @RequestParam(required = false) String storage) {
    return ResponseEntity.ok(
        ApiResponse.success(
            cartService.removeItem(resolveUserId(principal), productId, color, storage),
            "Item removed"));
  }

  /** Merge guest (localStorage) cart with server cart on login. */
  @PostMapping("/sync")
  public ResponseEntity<ApiResponse<CartResponse>> syncCart(
      @AuthenticationPrincipal UserDetails principal, @RequestBody CartSyncRequest request) {
    return ResponseEntity.ok(
        ApiResponse.success(
            cartService.syncCart(resolveUserId(principal), request), "Cart synced"));
  }

  @DeleteMapping
  public ResponseEntity<ApiResponse<Void>> clearCart(
      @AuthenticationPrincipal UserDetails principal) {
    cartService.clearCart(resolveUserId(principal));
    return ResponseEntity.ok(ApiResponse.success(null, "Cart cleared"));
  }

  private String resolveUserId(UserDetails principal) {
    User user =
        userRepository
            .findByUsername(principal.getUsername())
            .orElseThrow(
                () -> new ResourceNotFoundException("User", "username", principal.getUsername()));
    return user.getId();
  }
}
