export type ChatMessageType = 'TEXT' | 'IMAGE';
export type ChatSenderRole = 'USER' | 'ADMIN';

export interface ChatConversation {
  id: string;
  userId: string;
  username: string;
  userEmail: string;
  lastMessagePreview: string;
  lastMessageSenderId: string;
  lastMessageSenderName: string;
  unreadCountForUser: number;
  unreadCountForAdmin: number;
  updatedAt: string;
}

export interface ChatMessage {
  id: string;
  conversationId: string;
  userId: string;
  senderId: string;
  senderName: string;
  senderRole: ChatSenderRole;
  type: ChatMessageType;
  content: string;
  readByUser: boolean;
  readByAdmin: boolean;
  createdAt: string;
}

export interface SendChatMessagePayload {
  conversationId?: string;
  userId?: string;
  type: ChatMessageType;
  content: string;
}
