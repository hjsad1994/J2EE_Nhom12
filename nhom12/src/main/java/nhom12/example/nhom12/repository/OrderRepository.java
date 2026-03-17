package nhom12.example.nhom12.repository;

import java.util.List;
import java.util.Optional;
import nhom12.example.nhom12.model.Order;
import nhom12.example.nhom12.model.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends MongoRepository<Order, String> {
  List<Order> findByUserIdOrderByCreatedAtDesc(String userId);

  Optional<Order> findByOrderCode(String orderCode);

  Page<Order> findAllByOrderByCreatedAtDesc(Pageable pageable);

  @Query("{ 'items.productId': ?0, 'status': { $in: ?1 } }")
  List<Order> findByProductIdAndStatusIn(String productId, List<OrderStatus> statuses);
}
