package nhom12.example.nhom12.repository;

import java.util.List;
import java.util.Optional;
import nhom12.example.nhom12.model.ChatConversation;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatConversationRepository extends MongoRepository<ChatConversation, String> {
  Optional<ChatConversation> findByUserId(String userId);

  List<ChatConversation> findAllByUserId(String userId, Sort sort);

  default java.util.List<ChatConversation> findAllSorted() {
    return findAll(Sort.by(Sort.Direction.DESC, "updatedAt"));
  }

  default Optional<ChatConversation> findLatestByUserId(String userId) {
    return findAllByUserId(userId, Sort.by(Sort.Direction.DESC, "updatedAt", "createdAt")).stream()
        .findFirst();
  }
}
