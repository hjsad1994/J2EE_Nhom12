package nhom12.example.nhom12.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nhom12.example.nhom12.model.ReviewAspectAnalysis;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AbsaPredictResponse {
  private List<ReviewAspectAnalysis> results;
}
