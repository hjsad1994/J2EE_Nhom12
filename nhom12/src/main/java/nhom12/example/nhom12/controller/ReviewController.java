package nhom12.example.nhom12.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import nhom12.example.nhom12.dto.request.CreateReviewRequest;
import nhom12.example.nhom12.dto.response.ApiResponse;
import nhom12.example.nhom12.dto.response.ReviewResponse;
import nhom12.example.nhom12.exception.ResourceNotFoundException;
import nhom12.example.nhom12.model.User;
import nhom12.example.nhom12.repository.UserRepository;
import nhom12.example.nhom12.service.ReviewService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

  private final ReviewService reviewService;
  private final UserRepository userRepository;

  @GetMapping
  public ResponseEntity<ApiResponse<List<ReviewResponse>>> getReviews(
      @RequestParam String productId) {
    return ResponseEntity.ok(
        ApiResponse.success(
            reviewService.getReviewsByProduct(productId), "Reviews retrieved successfully"));
  }

  @PostMapping
  public ResponseEntity<ApiResponse<ReviewResponse>> createReview(
      @AuthenticationPrincipal UserDetails principal,
      @Valid @RequestBody CreateReviewRequest request) {
    User user = resolveUser(principal);
    ReviewResponse review = reviewService.createReview(user.getId(), user.getUsername(), request);
    return new ResponseEntity<>(
        ApiResponse.created(review, "Review created successfully"), HttpStatus.CREATED);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse<Void>> deleteReview(
      @AuthenticationPrincipal UserDetails principal, @PathVariable String id) {
    User user = resolveUser(principal);
    reviewService.deleteReview(id, user.getId());
    return ResponseEntity.ok(ApiResponse.success(null, "Review deleted successfully"));
  }

  private User resolveUser(UserDetails principal) {
    return userRepository
        .findByUsername(principal.getUsername())
        .orElseThrow(
            () -> new ResourceNotFoundException("User", "username", principal.getUsername()));
  }
}
