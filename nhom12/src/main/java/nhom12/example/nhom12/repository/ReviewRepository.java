package nhom12.example.nhom12.repository;

import java.util.List;
import nhom12.example.nhom12.model.Review;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends MongoRepository<Review, String> {
  List<Review> findByProductIdOrderByCreatedAtDesc(String productId);

  boolean existsByUserIdAndProductId(String userId, String productId);
}
