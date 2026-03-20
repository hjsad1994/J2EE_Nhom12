package nhom12.example.nhom12.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import nhom12.example.nhom12.dto.request.SendChatMessageRequest;
import nhom12.example.nhom12.dto.response.ApiResponse;
import nhom12.example.nhom12.dto.response.ChatConversationResponse;
import nhom12.example.nhom12.dto.response.ChatMessageResponse;
import nhom12.example.nhom12.dto.response.ChatUnreadSummaryResponse;
import nhom12.example.nhom12.exception.ResourceNotFoundException;
import nhom12.example.nhom12.model.User;
import nhom12.example.nhom12.repository.UserRepository;
import nhom12.example.nhom12.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

  private final ChatService chatService;
  private final UserRepository userRepository;

  @GetMapping("/my/conversation")
  @PreAuthorize("hasRole('USER')")
  public ResponseEntity<ApiResponse<ChatConversationResponse>> getMyConversation(
      @AuthenticationPrincipal UserDetails principal) {
    return ResponseEntity.ok(
        ApiResponse.success(
            chatService.getOrCreateMyConversation(resolveUserId(principal)),
            "Conversation retrieved successfully"));
  }

  @GetMapping("/my/unread-summary")
  @PreAuthorize("hasRole('USER')")
  public ResponseEntity<ApiResponse<ChatUnreadSummaryResponse>> getMyUnreadSummary(
      @AuthenticationPrincipal UserDetails principal) {
    return ResponseEntity.ok(
        ApiResponse.success(
            chatService.getUnreadSummaryForUser(resolveUserId(principal)),
            "Unread summary retrieved successfully"));
  }

  @GetMapping("/my/messages")
  @PreAuthorize("hasRole('USER')")
  public ResponseEntity<ApiResponse<List<ChatMessageResponse>>> getMyMessages(
      @AuthenticationPrincipal UserDetails principal) {
    return ResponseEntity.ok(
        ApiResponse.success(
            chatService.getMyMessages(resolveUserId(principal)), "Messages retrieved successfully"));
  }

  @PostMapping("/my/messages")
  @PreAuthorize("hasRole('USER')")
  public ResponseEntity<ApiResponse<ChatMessageResponse>> sendMyMessage(
      @AuthenticationPrincipal UserDetails principal,
      @Valid @RequestBody SendChatMessageRequest request) {
    return ResponseEntity.ok(
        ApiResponse.success(
            chatService.sendMessageAsUser(resolveUserId(principal), request),
            "Message sent successfully"));
  }

  @GetMapping("/admin/conversations")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<List<ChatConversationResponse>>> getAdminConversations() {
    return ResponseEntity.ok(
        ApiResponse.success(
            chatService.getAdminConversations(), "Conversations retrieved successfully"));
  }

  @GetMapping("/admin/unread-summary")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<ChatUnreadSummaryResponse>> getAdminUnreadSummary() {
    return ResponseEntity.ok(
        ApiResponse.success(
            chatService.getUnreadSummaryForAdmin(), "Unread summary retrieved successfully"));
  }

  @GetMapping("/admin/conversations/{conversationId}/messages")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<List<ChatMessageResponse>>> getConversationMessages(
      @PathVariable String conversationId) {
    return ResponseEntity.ok(
        ApiResponse.success(
            chatService.getConversationMessagesForAdmin(conversationId),
            "Messages retrieved successfully"));
  }

  @PostMapping("/admin/messages")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<ChatMessageResponse>> sendAdminMessage(
      @AuthenticationPrincipal UserDetails principal,
      @Valid @RequestBody SendChatMessageRequest request) {
    return ResponseEntity.ok(
        ApiResponse.success(
            chatService.sendMessageAsAdmin(resolveUserId(principal), request),
            "Message sent successfully"));
  }

  private String resolveUserId(UserDetails principal) {
    User user =
        userRepository
            .findByUsername(principal.getUsername())
            .orElseThrow(
                () -> new ResourceNotFoundException("User", "username", principal.getUsername()));
    return user.getId();
  }
}
