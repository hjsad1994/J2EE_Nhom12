package nhom12.example.nhom12.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nhom12.example.nhom12.model.enums.ChatMessageType;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SendChatMessageRequest {
  private String conversationId;
  private String userId;
  @NotNull private ChatMessageType type;
  private String content;
}
