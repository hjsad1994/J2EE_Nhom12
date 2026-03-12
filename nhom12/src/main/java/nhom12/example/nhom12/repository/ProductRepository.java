package nhom12.example.nhom12.repository;

import java.util.List;
import nhom12.example.nhom12.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends MongoRepository<Product, String> {
  Page<Product> findByCategoryId(String categoryId, Pageable pageable);

  long countByCategoryId(String categoryId);

  List<Product> findByCategoryId(String categoryId);
}
