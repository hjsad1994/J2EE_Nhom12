package nhom12.example.nhom12.repository;

import java.util.List;
import nhom12.example.nhom12.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends MongoRepository<Order, String> {
  List<Order> findByUserIdOrderByCreatedAtDesc(String userId);

  Page<Order> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
