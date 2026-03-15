package nhom12.example.nhom12.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nhom12.example.nhom12.model.ReviewAspectAnalysis;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewResponse {
  private String id;
  private String productId;
  private String userId;
  private String username;
  private int rating;
  private String comment;
  private List<String> images;
  private List<ReviewAspectAnalysis> analysisResults;
  private LocalDateTime createdAt;
}
