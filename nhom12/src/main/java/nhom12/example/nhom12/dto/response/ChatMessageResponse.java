package nhom12.example.nhom12.dto.response;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nhom12.example.nhom12.model.enums.ChatMessageType;
import nhom12.example.nhom12.model.enums.Role;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageResponse {
  private String id;
  private String conversationId;
  private String userId;
  private String senderId;
  private String senderName;
  private Role senderRole;
  private ChatMessageType type;
  private String content;
  private boolean readByUser;
  private boolean readByAdmin;
  private LocalDateTime createdAt;
}
