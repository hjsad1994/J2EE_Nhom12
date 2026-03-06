package nhom12.example.nhom12.service;

import java.util.List;
import nhom12.example.nhom12.dto.request.CreateReviewRequest;
import nhom12.example.nhom12.dto.response.ReviewResponse;

public interface ReviewService {
  List<ReviewResponse> getReviewsByProduct(String productId);

  ReviewResponse createReview(String userId, String username, CreateReviewRequest request);

  void deleteReview(String reviewId, String userId);
}
