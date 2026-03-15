package nhom12.example.nhom12.service;

import java.util.List;
import nhom12.example.nhom12.model.ReviewAspectAnalysis;

public interface AbsaService {
  List<ReviewAspectAnalysis> analyzeComment(String comment);
}
