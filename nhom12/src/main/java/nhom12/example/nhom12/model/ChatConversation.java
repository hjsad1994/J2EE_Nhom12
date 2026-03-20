package nhom12.example.nhom12.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "chat_conversations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ChatConversation extends BaseDocument {

  @Indexed(unique = true)
  private String userId;

  private String username;
  private String userEmail;
  private String lastMessagePreview;
  private String lastMessageSenderId;
  private String lastMessageSenderName;
  private int unreadCountForUser;
  private int unreadCountForAdmin;
}
