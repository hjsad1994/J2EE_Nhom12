package nhom12.example.nhom12.dto.response;

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
public class ChatUnreadSummaryResponse {
  private int unreadCountForUser;
  private int unreadCountForAdmin;
}
