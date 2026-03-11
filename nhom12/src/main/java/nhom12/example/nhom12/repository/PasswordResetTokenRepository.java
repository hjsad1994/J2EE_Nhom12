package nhom12.example.nhom12.repository;

import java.util.Optional;
import nhom12.example.nhom12.model.PasswordResetToken;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PasswordResetTokenRepository extends MongoRepository<PasswordResetToken, String> {

  Optional<PasswordResetToken> findByToken(String token);

  void deleteByUserId(String userId);
}
