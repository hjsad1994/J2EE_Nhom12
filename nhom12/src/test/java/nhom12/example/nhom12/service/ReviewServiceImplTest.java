package nhom12.example.nhom12.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import nhom12.example.nhom12.dto.request.CreateReviewRequest;
import nhom12.example.nhom12.dto.response.ReviewResponse;
import nhom12.example.nhom12.exception.BadRequestException;
import nhom12.example.nhom12.exception.DuplicateResourceException;
import nhom12.example.nhom12.exception.ResourceNotFoundException;
import nhom12.example.nhom12.model.Product;
import nhom12.example.nhom12.model.Review;
import nhom12.example.nhom12.repository.ProductRepository;
import nhom12.example.nhom12.repository.ReviewRepository;
import nhom12.example.nhom12.service.impl.ReviewServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReviewServiceImpl - Unit Tests")
class ReviewServiceImplTest {

  @Mock private ReviewRepository reviewRepository;
  @Mock private ProductRepository productRepository;
  @Mock private AbsaService absaService;

  @InjectMocks private ReviewServiceImpl reviewService;

  private static final String USER_ID = "user001";
  private static final String USERNAME = "nguyenvana";
  private static final String PRODUCT_ID = "prod001";
  private static final String REVIEW_ID = "review001";

  private CreateReviewRequest reviewRequest;
  private Review savedReview;
  private Product product;

  @BeforeEach
  void setUp() {
    reviewRequest = new CreateReviewRequest(PRODUCT_ID, 5, "Sản phẩm rất tốt!", List.of());

    savedReview = Review.builder()
        .userId(USER_ID)
        .username(USERNAME)
        .productId(PRODUCT_ID)
        .rating(5)
        .comment("Sản phẩm rất tốt!")
        .images(List.of())
        .analysisResults(List.of())
        .build();
    savedReview.setId(REVIEW_ID);

    product = new Product();
    product.setId(PRODUCT_ID);
    product.setName("iPhone 15");
    product.setRating(0.0);
  }

  // ───────────────────────────────────────────────────
  // GET REVIEWS BY PRODUCT
  // ───────────────────────────────────────────────────
  @Nested
  @DisplayName("getReviewsByProduct()")
  class GetReviewsByProduct {

    @Test
    @DisplayName("Lấy danh sách review theo sản phẩm")
    void getReviewsByProduct_returnsReviewList() {
      when(reviewRepository.findByProductIdOrderByCreatedAtDesc(PRODUCT_ID))
          .thenReturn(List.of(savedReview));

      List<ReviewResponse> result = reviewService.getReviewsByProduct(PRODUCT_ID);

      assertThat(result).hasSize(1);
      assertThat(result.get(0).getProductId()).isEqualTo(PRODUCT_ID);
      assertThat(result.get(0).getRating()).isEqualTo(5);
    }

    @Test
    @DisplayName("Sản phẩm không có review - trả về danh sách rỗng")
    void getReviewsByProduct_noReviews_returnsEmptyList() {
      when(reviewRepository.findByProductIdOrderByCreatedAtDesc(PRODUCT_ID))
          .thenReturn(List.of());

      List<ReviewResponse> result = reviewService.getReviewsByProduct(PRODUCT_ID);

      assertThat(result).isEmpty();
    }
  }

  // ───────────────────────────────────────────────────
  // CREATE REVIEW
  // ───────────────────────────────────────────────────
  @Nested
  @DisplayName("createReview()")
  class CreateReview {

    @Test
    @DisplayName("Tạo review thành công - ABSA được gọi và rating được cập nhật")
    void createReview_success_analyzesAndUpdatesRating() {
      when(productRepository.existsById(PRODUCT_ID)).thenReturn(true);
      when(reviewRepository.existsByUserIdAndProductId(USER_ID, PRODUCT_ID)).thenReturn(false);
      when(absaService.analyzeComment("Sản phẩm rất tốt!")).thenReturn(List.of());
      when(reviewRepository.save(any(Review.class))).thenReturn(savedReview);
      when(reviewRepository.findByProductIdOrderByCreatedAtDesc(PRODUCT_ID))
          .thenReturn(List.of(savedReview));
      when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
      when(productRepository.save(any(Product.class))).thenReturn(product);

      ReviewResponse result = reviewService.createReview(USER_ID, USERNAME, reviewRequest);

      assertThat(result).isNotNull();
      assertThat(result.getUserId()).isEqualTo(USER_ID);
      assertThat(result.getRating()).isEqualTo(5);
      assertThat(result.getComment()).isEqualTo("Sản phẩm rất tốt!");
      verify(absaService).analyzeComment("Sản phẩm rất tốt!");
      verify(productRepository).save(any(Product.class)); // rating recalculated
    }

    @Test
    @DisplayName("Sản phẩm không tồn tại → ném ResourceNotFoundException")
    void createReview_productNotFound_throwsResourceNotFoundException() {
      when(productRepository.existsById(PRODUCT_ID)).thenReturn(false);

      assertThatThrownBy(() -> reviewService.createReview(USER_ID, USERNAME, reviewRequest))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessageContaining("Product");
    }

    @Test
    @DisplayName("Người dùng đã review sản phẩm này rồi → ném DuplicateResourceException")
    void createReview_duplicateReview_throwsDuplicateResourceException() {
      when(productRepository.existsById(PRODUCT_ID)).thenReturn(true);
      when(reviewRepository.existsByUserIdAndProductId(USER_ID, PRODUCT_ID)).thenReturn(true);

      assertThatThrownBy(() -> reviewService.createReview(USER_ID, USERNAME, reviewRequest))
          .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    @DisplayName("Tạo review - rating sản phẩm được tính lại đúng")
    void createReview_recalculatesProductRating() {
      Review review1 = Review.builder().rating(4).productId(PRODUCT_ID).userId("u1").build();
      Review review2 = Review.builder().rating(5).productId(PRODUCT_ID).userId("u2").build();

      when(productRepository.existsById(PRODUCT_ID)).thenReturn(true);
      when(reviewRepository.existsByUserIdAndProductId(USER_ID, PRODUCT_ID)).thenReturn(false);
      when(absaService.analyzeComment(any())).thenReturn(List.of());
      when(reviewRepository.save(any(Review.class))).thenReturn(savedReview);
      // After save, the 3 reviews: 4, 5, 5 → avg = 4.67 → rounded = 4.7
      when(reviewRepository.findByProductIdOrderByCreatedAtDesc(PRODUCT_ID))
          .thenReturn(List.of(review1, review2, savedReview));
      when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
      when(productRepository.save(any(Product.class))).thenReturn(product);

      reviewService.createReview(USER_ID, USERNAME, reviewRequest);

      // rating = (4 + 5 + 5) / 3 = 4.67 → Math.round(4.67 * 10) / 10 = 4.7
      assertThat(product.getRating()).isEqualTo(4.7);
    }
  }

  // ───────────────────────────────────────────────────
  // UPDATE REVIEW
  // ───────────────────────────────────────────────────
  @Nested
  @DisplayName("updateReview()")
  class UpdateReview {

    @Test
    @DisplayName("Cập nhật review thành công")
    void updateReview_success_updatesFields() {
      CreateReviewRequest updateReq = new CreateReviewRequest(PRODUCT_ID, 3, "Bình thường thôi", List.of());

      when(reviewRepository.findById(REVIEW_ID)).thenReturn(Optional.of(savedReview));
      when(absaService.analyzeComment("Bình thường thôi")).thenReturn(List.of());
      when(reviewRepository.save(any(Review.class))).thenReturn(savedReview);
      when(reviewRepository.findByProductIdOrderByCreatedAtDesc(PRODUCT_ID))
          .thenReturn(List.of(savedReview));
      when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
      when(productRepository.save(any(Product.class))).thenReturn(product);

      ReviewResponse result = reviewService.updateReview(REVIEW_ID, USER_ID, updateReq);

      assertThat(savedReview.getRating()).isEqualTo(3);
      assertThat(savedReview.getComment()).isEqualTo("Bình thường thôi");
    }

    @Test
    @DisplayName("Cập nhật review của người khác → ném BadRequestException")
    void updateReview_wrongUser_throwsBadRequestException() {
      when(reviewRepository.findById(REVIEW_ID)).thenReturn(Optional.of(savedReview));

      assertThatThrownBy(() -> reviewService.updateReview(REVIEW_ID, "other_user", reviewRequest))
          .isInstanceOf(BadRequestException.class)
          .hasMessageContaining("your own reviews");
    }

    @Test
    @DisplayName("Cập nhật review với productId khác → ném BadRequestException")
    void updateReview_differentProductId_throwsBadRequestException() {
      CreateReviewRequest wrongProductReq = new CreateReviewRequest("different_prod", 4, "OK", List.of());
      when(reviewRepository.findById(REVIEW_ID)).thenReturn(Optional.of(savedReview));

      assertThatThrownBy(() -> reviewService.updateReview(REVIEW_ID, USER_ID, wrongProductReq))
          .isInstanceOf(BadRequestException.class)
          .hasMessageContaining("Product ID does not match");
    }

    @Test
    @DisplayName("Review không tồn tại → ném ResourceNotFoundException")
    void updateReview_notFound_throwsResourceNotFoundException() {
      when(reviewRepository.findById("bad_id")).thenReturn(Optional.empty());

      assertThatThrownBy(() -> reviewService.updateReview("bad_id", USER_ID, reviewRequest))
          .isInstanceOf(ResourceNotFoundException.class);
    }
  }

  // ───────────────────────────────────────────────────
  // DELETE REVIEW
  // ───────────────────────────────────────────────────
  @Nested
  @DisplayName("deleteReview()")
  class DeleteReview {

    @Test
    @DisplayName("Xóa review thành công - rating được tính lại")
    void deleteReview_success_recalculatesRating() {
      when(reviewRepository.findById(REVIEW_ID)).thenReturn(Optional.of(savedReview));
      when(reviewRepository.findByProductIdOrderByCreatedAtDesc(PRODUCT_ID)).thenReturn(List.of());
      when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
      when(productRepository.save(any(Product.class))).thenReturn(product);

      reviewService.deleteReview(REVIEW_ID, USER_ID);

      verify(reviewRepository).deleteById(REVIEW_ID);
      // No reviews left → rating should be 0.0
      assertThat(product.getRating()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Xóa review của người khác → ném BadRequestException")
    void deleteReview_wrongUser_throwsBadRequestException() {
      when(reviewRepository.findById(REVIEW_ID)).thenReturn(Optional.of(savedReview));

      assertThatThrownBy(() -> reviewService.deleteReview(REVIEW_ID, "attacker"))
          .isInstanceOf(BadRequestException.class)
          .hasMessageContaining("your own reviews");

      verify(reviewRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Review không tồn tại → ném ResourceNotFoundException")
    void deleteReview_notFound_throwsResourceNotFoundException() {
      when(reviewRepository.findById("bad_id")).thenReturn(Optional.empty());

      assertThatThrownBy(() -> reviewService.deleteReview("bad_id", USER_ID))
          .isInstanceOf(ResourceNotFoundException.class);
    }
  }
}
