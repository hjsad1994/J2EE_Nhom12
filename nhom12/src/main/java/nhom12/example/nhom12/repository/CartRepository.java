package nhom12.example.nhom12.repository;

import java.util.Optional;
import nhom12.example.nhom12.model.Cart;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartRepository extends MongoRepository<Cart, String> {
  Optional<Cart> findByUserId(String userId);
}
