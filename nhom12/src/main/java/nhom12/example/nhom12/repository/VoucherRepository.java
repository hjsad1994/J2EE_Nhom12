package nhom12.example.nhom12.repository;

import java.util.Optional;
import nhom12.example.nhom12.model.Voucher;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VoucherRepository extends MongoRepository<Voucher, String> {
  Optional<Voucher> findByCodeIgnoreCase(String code);

  default java.util.List<Voucher> findAllSorted() {
    return findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
  }
}
