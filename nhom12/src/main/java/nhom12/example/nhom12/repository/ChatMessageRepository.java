package nhom12.example.nhom12.repository;

import java.util.List;
import nhom12.example.nhom12.model.ChatMessage;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {
  List<ChatMessage> findByConversationId(String conversationId, Sort sort);

  long countByConversationIdAndReadByAdminFalse(String conversationId);

  long countByConversationIdAndReadByUserFalse(String conversationId);
}
