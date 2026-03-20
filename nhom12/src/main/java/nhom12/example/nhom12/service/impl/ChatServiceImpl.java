package nhom12.example.nhom12.service.impl;

import java.util.List;
import lombok.RequiredArgsConstructor;
import nhom12.example.nhom12.dto.request.SendChatMessageRequest;
import nhom12.example.nhom12.dto.response.ChatConversationResponse;
import nhom12.example.nhom12.dto.response.ChatMessageResponse;
import nhom12.example.nhom12.dto.response.ChatUnreadSummaryResponse;
import nhom12.example.nhom12.exception.BadRequestException;
import nhom12.example.nhom12.exception.ResourceNotFoundException;
import nhom12.example.nhom12.model.ChatConversation;
import nhom12.example.nhom12.model.ChatMessage;
import nhom12.example.nhom12.model.User;
import nhom12.example.nhom12.model.enums.ChatMessageType;
import nhom12.example.nhom12.model.enums.Role;
import nhom12.example.nhom12.repository.ChatConversationRepository;
import nhom12.example.nhom12.repository.ChatMessageRepository;
import nhom12.example.nhom12.repository.UserRepository;
import nhom12.example.nhom12.service.ChatService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

  private static final Sort MESSAGE_SORT = Sort.by(Sort.Direction.ASC, "createdAt");

  private final ChatConversationRepository chatConversationRepository;
  private final ChatMessageRepository chatMessageRepository;
  private final UserRepository userRepository;
  private final SimpMessagingTemplate messagingTemplate;

  @Override
  public ChatConversationResponse getOrCreateMyConversation(String userId) {
    ChatConversation conversation = getOrCreateConversation(userId);
    markMessagesAsReadForUser(conversation);
    return toConversationResponse(chatConversationRepository.save(conversation));
  }

  @Override
  public List<ChatConversationResponse> getAdminConversations() {
    return chatConversationRepository.findAllSorted().stream().map(this::toConversationResponse).toList();
  }

  @Override
  public List<ChatMessageResponse> getMyMessages(String userId) {
    ChatConversation conversation = getOrCreateConversation(userId);
    markMessagesAsReadForUser(conversation);
    return chatMessageRepository.findByConversationId(conversation.getId(), MESSAGE_SORT).stream()
        .map(this::toMessageResponse)
        .toList();
  }

  @Override
  public List<ChatMessageResponse> getConversationMessagesForAdmin(String conversationId) {
    ChatConversation conversation =
        chatConversationRepository
            .findById(conversationId)
            .orElseThrow(() -> new ResourceNotFoundException("Conversation", "id", conversationId));
    markMessagesAsReadForAdmin(conversation);
    return chatMessageRepository.findByConversationId(conversation.getId(), MESSAGE_SORT).stream()
        .map(this::toMessageResponse)
        .toList();
  }

  @Override
  @Transactional
  public ChatMessageResponse sendMessageAsUser(String userId, SendChatMessageRequest request) {
    ChatConversation conversation = getOrCreateConversation(userId);
    User user = getUserById(userId);
    ChatMessage message =
        buildMessage(conversation, user, Role.USER, request.getType(), normalizeContent(request));
    ChatMessage savedMessage = chatMessageRepository.save(message);

    conversation.setLastMessagePreview(buildPreview(savedMessage));
    conversation.setLastMessageSenderId(savedMessage.getSenderId());
    conversation.setLastMessageSenderName(savedMessage.getSenderName());
    conversation.setUnreadCountForAdmin(
        (int) chatMessageRepository.countByConversationIdAndReadByAdminFalse(conversation.getId()));
    conversation.setUnreadCountForUser(0);
    ChatConversation savedConversation = chatConversationRepository.save(conversation);

    ChatMessageResponse response = toMessageResponse(savedMessage);
    broadcastConversationUpdate(savedConversation);
    broadcastMessage(response);
    return response;
  }

  @Override
  @Transactional
  public ChatMessageResponse sendMessageAsAdmin(String adminId, SendChatMessageRequest request) {
    if (request.getConversationId() == null || request.getConversationId().isBlank()) {
      throw new BadRequestException("conversationId là bắt buộc khi admin gửi tin nhắn");
    }

    ChatConversation conversation =
        chatConversationRepository
            .findById(request.getConversationId())
            .orElseThrow(
                () -> new ResourceNotFoundException("Conversation", "id", request.getConversationId()));
    User admin = getUserById(adminId);
    ChatMessage message =
        buildMessage(conversation, admin, Role.ADMIN, request.getType(), normalizeContent(request));
    ChatMessage savedMessage = chatMessageRepository.save(message);

    conversation.setLastMessagePreview(buildPreview(savedMessage));
    conversation.setLastMessageSenderId(savedMessage.getSenderId());
    conversation.setLastMessageSenderName(savedMessage.getSenderName());
    conversation.setUnreadCountForUser(
        (int) chatMessageRepository.countByConversationIdAndReadByUserFalse(conversation.getId()));
    conversation.setUnreadCountForAdmin(0);
    ChatConversation savedConversation = chatConversationRepository.save(conversation);

    ChatMessageResponse response = toMessageResponse(savedMessage);
    broadcastConversationUpdate(savedConversation);
    broadcastMessage(response);
    return response;
  }

  @Override
  public ChatUnreadSummaryResponse getUnreadSummaryForUser(String userId) {
    int unreadCount =
        findLatestConversationByUserId(userId).map(ChatConversation::getUnreadCountForUser).orElse(0);
    return ChatUnreadSummaryResponse.builder()
        .unreadCountForUser(unreadCount)
        .unreadCountForAdmin(0)
        .build();
  }

  @Override
  public ChatUnreadSummaryResponse getUnreadSummaryForAdmin() {
    int unreadCount =
        chatConversationRepository.findAll().stream().mapToInt(ChatConversation::getUnreadCountForAdmin).sum();
    return ChatUnreadSummaryResponse.builder()
        .unreadCountForUser(0)
        .unreadCountForAdmin(unreadCount)
        .build();
  }

  private ChatConversation getOrCreateConversation(String userId) {
    return chatConversationRepository
        .findLatestByUserId(userId)
        .orElseGet(
            () -> {
              User user = getUserById(userId);
              try {
                return chatConversationRepository.save(
                    ChatConversation.builder()
                        .userId(user.getId())
                        .username(user.getUsername())
                        .userEmail(user.getEmail())
                        .lastMessagePreview("")
                        .lastMessageSenderId("")
                        .lastMessageSenderName("")
                        .unreadCountForUser(0)
                        .unreadCountForAdmin(0)
                        .build());
              } catch (DuplicateKeyException ex) {
                return chatConversationRepository
                    .findLatestByUserId(userId)
                    .orElseThrow(
                        () ->
                            new BadRequestException(
                                "Không thể tạo hội thoại chat. Vui lòng thử lại."));
              }
            });
  }

  private java.util.Optional<ChatConversation> findLatestConversationByUserId(String userId) {
    return chatConversationRepository.findLatestByUserId(userId);
  }

  private User getUserById(String id) {
    return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
  }

  private ChatMessage buildMessage(
      ChatConversation conversation, User sender, Role senderRole, ChatMessageType type, String content) {
    validateMessage(type, content);
    boolean fromUser = senderRole == Role.USER;

    return ChatMessage.builder()
        .conversationId(conversation.getId())
        .userId(conversation.getUserId())
        .senderId(sender.getId())
        .senderName(sender.getUsername())
        .senderRole(senderRole)
        .type(type)
        .content(content)
        .readByUser(fromUser)
        .readByAdmin(!fromUser)
        .build();
  }

  private void validateMessage(ChatMessageType type, String content) {
    if (content == null || content.isBlank()) {
      throw new BadRequestException("Nội dung tin nhắn không được để trống");
    }
    if (type == ChatMessageType.IMAGE && !content.startsWith("http")) {
      throw new BadRequestException("Tin nhắn ảnh phải là URL hợp lệ");
    }
  }

  private String normalizeContent(SendChatMessageRequest request) {
    return request.getContent() == null ? "" : request.getContent().trim();
  }

  private String buildPreview(ChatMessage message) {
    return message.getType() == ChatMessageType.IMAGE ? "[Hình ảnh]" : message.getContent();
  }

  private void markMessagesAsReadForUser(ChatConversation conversation) {
    List<ChatMessage> messages = chatMessageRepository.findByConversationId(conversation.getId(), MESSAGE_SORT);
    boolean changed = false;
    for (ChatMessage message : messages) {
      if (!message.isReadByUser()) {
        message.setReadByUser(true);
        ChatMessage savedMessage = chatMessageRepository.save(message);
        broadcastMessage(toMessageResponse(savedMessage));
        changed = true;
      }
    }
    if (conversation.getUnreadCountForUser() != 0) {
      conversation.setUnreadCountForUser(0);
      changed = true;
    }
    if (changed) {
      chatConversationRepository.save(conversation);
      broadcastConversationUpdate(conversation);
    }
  }

  private void markMessagesAsReadForAdmin(ChatConversation conversation) {
    List<ChatMessage> messages = chatMessageRepository.findByConversationId(conversation.getId(), MESSAGE_SORT);
    boolean changed = false;
    for (ChatMessage message : messages) {
      if (!message.isReadByAdmin()) {
        message.setReadByAdmin(true);
        ChatMessage savedMessage = chatMessageRepository.save(message);
        broadcastMessage(toMessageResponse(savedMessage));
        changed = true;
      }
    }
    if (conversation.getUnreadCountForAdmin() != 0) {
      conversation.setUnreadCountForAdmin(0);
      changed = true;
    }
    if (changed) {
      chatConversationRepository.save(conversation);
      broadcastConversationUpdate(conversation);
    }
  }

  private void broadcastConversationUpdate(ChatConversation conversation) {
    ChatConversationResponse payload = toConversationResponse(conversation);
    messagingTemplate.convertAndSend("/topic/admin/chat/conversations", payload);
    messagingTemplate.convertAndSendToUser(conversation.getUserId(), "/queue/chat/conversation", payload);
  }

  private void broadcastMessage(ChatMessageResponse message) {
    messagingTemplate.convertAndSend("/topic/admin/chat/messages", message);
    messagingTemplate.convertAndSendToUser(message.getUserId(), "/queue/chat/messages", message);
  }

  private ChatConversationResponse toConversationResponse(ChatConversation conversation) {
    return ChatConversationResponse.builder()
        .id(conversation.getId())
        .userId(conversation.getUserId())
        .username(conversation.getUsername())
        .userEmail(conversation.getUserEmail())
        .lastMessagePreview(conversation.getLastMessagePreview())
        .lastMessageSenderId(conversation.getLastMessageSenderId())
        .lastMessageSenderName(conversation.getLastMessageSenderName())
        .unreadCountForUser(conversation.getUnreadCountForUser())
        .unreadCountForAdmin(conversation.getUnreadCountForAdmin())
        .updatedAt(conversation.getUpdatedAt())
        .build();
  }

  private ChatMessageResponse toMessageResponse(ChatMessage message) {
    return ChatMessageResponse.builder()
        .id(message.getId())
        .conversationId(message.getConversationId())
        .userId(message.getUserId())
        .senderId(message.getSenderId())
        .senderName(message.getSenderName())
        .senderRole(message.getSenderRole())
        .type(message.getType())
        .content(message.getContent())
        .readByUser(message.isReadByUser())
        .readByAdmin(message.isReadByAdmin())
        .createdAt(message.getCreatedAt())
        .build();
  }
}
