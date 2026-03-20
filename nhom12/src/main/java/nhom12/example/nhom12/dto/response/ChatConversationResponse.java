package nhom12.example.nhom12.dto.response;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatConversationResponse {
  private String id;
  private String userId;
  private String username;
  private String userEmail;
  private String lastMessagePreview;
  private String lastMessageSenderId;
  private String lastMessageSenderName;
  private int unreadCountForUser;
  private int unreadCountForAdmin;
  private LocalDateTime updatedAt;
}
