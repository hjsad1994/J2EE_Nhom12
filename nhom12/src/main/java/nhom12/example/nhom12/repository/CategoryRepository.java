package nhom12.example.nhom12.repository;

import java.util.Optional;
import nhom12.example.nhom12.model.Category;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends MongoRepository<Category, String> {
  Optional<Category> findBySlug(String slug);

  boolean existsByName(String name);
}
