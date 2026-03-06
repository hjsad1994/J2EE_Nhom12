package nhom12.example.nhom12.repository;

import java.util.Optional;
import nhom12.example.nhom12.model.Wishlist;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WishlistRepository extends MongoRepository<Wishlist, String> {
  Optional<Wishlist> findByUserId(String userId);
}
