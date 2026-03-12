package nhom12.example.nhom12.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import nhom12.example.nhom12.dto.response.ApiResponse;
import nhom12.example.nhom12.dto.response.ProductResponse;
import nhom12.example.nhom12.exception.ResourceNotFoundException;
import nhom12.example.nhom12.model.User;
import nhom12.example.nhom12.repository.UserRepository;
import nhom12.example.nhom12.service.WishlistService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class WishlistController {

  private final WishlistService wishlistService;
  private final UserRepository userRepository;

  @GetMapping
  public ResponseEntity<ApiResponse<List<ProductResponse>>> getWishlist(
      @AuthenticationPrincipal UserDetails principal) {
    String userId = resolveUserId(principal);
    return ResponseEntity.ok(
        ApiResponse.success(
            wishlistService.getWishlist(userId), "Wishlist retrieved successfully"));
  }

  @PostMapping("/{productId}")
  public ResponseEntity<ApiResponse<List<ProductResponse>>> toggleProduct(
      @AuthenticationPrincipal UserDetails principal, @PathVariable String productId) {
    String userId = resolveUserId(principal);
    return ResponseEntity.ok(
        ApiResponse.success(
            wishlistService.toggleProduct(userId, productId), "Wishlist updated successfully"));
  }

  @DeleteMapping
  public ResponseEntity<ApiResponse<List<ProductResponse>>> clearWishlist(
      @AuthenticationPrincipal UserDetails principal) {
    String userId = resolveUserId(principal);
    return ResponseEntity.ok(
        ApiResponse.success(
            wishlistService.clearWishlist(userId), "Wishlist cleared successfully"));
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
