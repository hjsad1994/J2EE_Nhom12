package nhom12.example.nhom12.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import nhom12.example.nhom12.model.enums.ChatMessageType;
import nhom12.example.nhom12.model.enums.Role;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "chat_messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ChatMessage extends BaseDocument {

  @Indexed private String conversationId;
  @Indexed private String userId;
  private String senderId;
  private String senderName;
  private Role senderRole;
  private ChatMessageType type;
  private String content;
  private boolean readByUser;
  private boolean readByAdmin;
}
