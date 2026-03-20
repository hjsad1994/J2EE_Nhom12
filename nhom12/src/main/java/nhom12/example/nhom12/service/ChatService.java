package nhom12.example.nhom12.service;

import java.util.List;
import nhom12.example.nhom12.dto.request.SendChatMessageRequest;
import nhom12.example.nhom12.dto.response.ChatConversationResponse;
import nhom12.example.nhom12.dto.response.ChatMessageResponse;
import nhom12.example.nhom12.dto.response.ChatUnreadSummaryResponse;

public interface ChatService {

  ChatConversationResponse getOrCreateMyConversation(String userId);

  List<ChatConversationResponse> getAdminConversations();

  List<ChatMessageResponse> getMyMessages(String userId);

  List<ChatMessageResponse> getConversationMessagesForAdmin(String conversationId);

  ChatMessageResponse sendMessageAsUser(String userId, SendChatMessageRequest request);

  ChatMessageResponse sendMessageAsAdmin(String adminId, SendChatMessageRequest request);

  ChatUnreadSummaryResponse getUnreadSummaryForUser(String userId);

  ChatUnreadSummaryResponse getUnreadSummaryForAdmin();
}
