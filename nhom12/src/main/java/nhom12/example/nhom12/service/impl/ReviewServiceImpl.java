package nhom12.example.nhom12.service.impl;

import java.util.List;
import lombok.RequiredArgsConstructor;
import nhom12.example.nhom12.dto.request.CreateReviewRequest;
import nhom12.example.nhom12.dto.response.ReviewResponse;
import nhom12.example.nhom12.exception.BadRequestException;
import nhom12.example.nhom12.exception.DuplicateResourceException;
import nhom12.example.nhom12.exception.ResourceNotFoundException;
import nhom12.example.nhom12.model.Review;
import nhom12.example.nhom12.repository.ProductRepository;
import nhom12.example.nhom12.repository.ReviewRepository;
import nhom12.example.nhom12.service.ReviewService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

  private final ReviewRepository reviewRepository;
  private final ProductRepository productRepository;

  @Override
  public List<ReviewResponse> getReviewsByProduct(String productId) {
    return reviewRepository.findByProductIdOrderByCreatedAtDesc(productId).stream()
        .map(this::toResponse)
        .toList();
  }

  @Override
  public ReviewResponse createReview(String userId, String username, CreateReviewRequest request) {
    if (!productRepository.existsById(request.getProductId())) {
      throw new ResourceNotFoundException("Product", "id", request.getProductId());
    }
    if (reviewRepository.existsByUserIdAndProductId(userId, request.getProductId())) {
      throw new DuplicateResourceException("Review", "user", request.getProductId());
    }

    Review review =
        Review.builder()
            .userId(userId)
            .username(username)
            .productId(request.getProductId())
            .rating(request.getRating())
            .comment(request.getComment())
            .build();

    Review saved = reviewRepository.save(review);
    recalculateProductRating(request.getProductId());
    return toResponse(saved);
  }

  @Override
  public void deleteReview(String reviewId, String userId) {
    Review review =
        reviewRepository
            .findById(reviewId)
            .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));

    if (!review.getUserId().equals(userId)) {
      throw new BadRequestException("You can only delete your own reviews");
    }

    String productId = review.getProductId();
    reviewRepository.deleteById(reviewId);
    recalculateProductRating(productId);
  }

  private void recalculateProductRating(String productId) {
    List<Review> reviews = reviewRepository.findByProductIdOrderByCreatedAtDesc(productId);
    double avg =
        reviews.isEmpty()
            ? 0.0
            : reviews.stream().mapToInt(Review::getRating).average().orElse(0.0);
    double rounded = Math.round(avg * 10.0) / 10.0;

    productRepository
        .findById(productId)
        .ifPresent(
            p -> {
              p.setRating(rounded);
              productRepository.save(p);
            });
  }

  private ReviewResponse toResponse(Review r) {
    return ReviewResponse.builder()
        .id(r.getId())
        .productId(r.getProductId())
        .userId(r.getUserId())
        .username(r.getUsername())
        .rating(r.getRating())
        .comment(r.getComment())
        .createdAt(r.getCreatedAt())
        .build();
  }
}
