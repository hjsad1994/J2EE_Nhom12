import { Client } from '@stomp/stompjs';
import {
  ImagePlus,
  Loader2,
  MessageCircle,
  SendHorizontal,
  X,
} from 'lucide-react';
import { AnimatePresence, motion } from 'motion/react';
import { useEffect, useMemo, useRef, useState } from 'react';

import apiClient from '@/api/client';
import { ENDPOINTS } from '@/api/endpoints';
import type { ApiResponse } from '@/api/types';
import { getBrokerUrl } from '@/lib/websocket';
import { useAuthStore } from '@/store/useAuthStore';
import { useChatStore } from '@/store/useChatStore';
import { useToastStore } from '@/store/useToastStore';
import type { ChatConversation, ChatMessage } from '@/types/chat';

const bubbleBase =
  'max-w-[78%] rounded-2xl px-3.5 py-2.5 text-sm shadow-sm break-words';

export default function ChatWidget() {
  const { token, user, isLoggedIn, isAdmin } = useAuthStore();
  const addToast = useToastStore((s) => s.addToast);
  const setUserUnreadCount = useChatStore((s) => s.setUserUnreadCount);
  const [open, setOpen] = useState(false);
  const [loading, setLoading] = useState(false);
  const [sending, setSending] = useState(false);
  const [uploading, setUploading] = useState(false);
  const [message, setMessage] = useState('');
  const [conversation, setConversation] = useState<ChatConversation | null>(null);
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [previewImage, setPreviewImage] = useState<string | null>(null);
  const clientRef = useRef<Client | null>(null);
  const bottomRef = useRef<HTMLDivElement | null>(null);

  const unreadCount = useMemo(
    () => (open ? 0 : conversation?.unreadCountForUser ?? 0),
    [conversation?.unreadCountForUser, open],
  );

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth', block: 'end' });
  }, [messages, open]);

  useEffect(() => {
    if (!isLoggedIn || isAdmin || !token || !user) return;

    const client = new Client({
      brokerURL: getBrokerUrl(),
      connectHeaders: {
        Authorization: `Bearer ${token}`,
      },
      reconnectDelay: 5000,
      onConnect: () => {
        client.subscribe('/user/queue/chat/messages', (frame) => {
          const nextMessage = JSON.parse(frame.body) as ChatMessage;
          setMessages((prev) =>
            prev.some((item) => item.id === nextMessage.id)
              ? prev.map((item) =>
                  item.id === nextMessage.id ? nextMessage : item,
                )
              : [...prev, nextMessage],
          );
          if (!open) {
            addToast('info', 'Bạn có tin nhắn mới từ hỗ trợ');
          }
        });

        client.subscribe('/user/queue/chat/conversation', (frame) => {
          const nextConversation = JSON.parse(frame.body) as ChatConversation;
          setConversation(nextConversation);
          setUserUnreadCount(nextConversation.unreadCountForUser ?? 0);
        });
      },
    });

    client.activate();
    clientRef.current = client;

    return () => {
      client.deactivate();
      clientRef.current = null;
    };
  }, [addToast, isAdmin, isLoggedIn, open, setUserUnreadCount, token, user]);

  useEffect(() => {
    if (!open || !isLoggedIn || isAdmin) return;

    const loadChat = async () => {
      setLoading(true);
      try {
        const conversationRes = await apiClient.get<ApiResponse<ChatConversation>>(
          ENDPOINTS.CHAT.MY_CONVERSATION,
        );
        setConversation(conversationRes.data.data);
        setUserUnreadCount(0);
        const messagesRes = await apiClient.get<ApiResponse<ChatMessage[]>>(
          ENDPOINTS.CHAT.MY_MESSAGES,
        );
        setMessages(messagesRes.data.data);
      } catch {
        addToast('error', 'Không thể tải khung chat');
      } finally {
        setLoading(false);
      }
    };

    void loadChat();
  }, [addToast, isAdmin, isLoggedIn, open, setUserUnreadCount]);

  if (!isLoggedIn || isAdmin) return null;

  const sendTextMessage = async () => {
    const content = message.trim();
    if (!content) return;

    setSending(true);
    try {
      const res = await apiClient.post<ApiResponse<ChatMessage>>(
        ENDPOINTS.CHAT.MY_MESSAGES,
        {
          type: 'TEXT',
          content,
        },
      );
      setMessages((prev) =>
        prev.some((item) => item.id === res.data.data.id)
          ? prev
          : [...prev, res.data.data],
      );
      setMessage('');
    } catch {
      addToast('error', 'Không thể gửi tin nhắn');
    } finally {
      setSending(false);
    }
  };

  const handleUploadImage = async (file: File) => {
    setUploading(true);
    try {
      const formData = new FormData();
      formData.append('file', file);
      const uploadRes = await apiClient.post<ApiResponse<string>>(
        ENDPOINTS.UPLOAD.CHAT_IMAGE,
        formData,
        {
          headers: { 'Content-Type': 'multipart/form-data' },
        },
      );
      const messageRes = await apiClient.post<ApiResponse<ChatMessage>>(
        ENDPOINTS.CHAT.MY_MESSAGES,
        {
          type: 'IMAGE',
          content: uploadRes.data.data,
        },
      );
      setMessages((prev) =>
        prev.some((item) => item.id === messageRes.data.data.id)
          ? prev
          : [...prev, messageRes.data.data],
      );
    } catch {
      addToast('error', 'Không thể gửi hình ảnh');
    } finally {
      setUploading(false);
    }
  };

  return (
    <>
      <button
        type="button"
        onClick={() => setOpen((prev) => !prev)}
        className="fixed right-5 bottom-5 z-50 flex h-14 w-14 cursor-pointer items-center justify-center rounded-full bg-brand text-white shadow-xl shadow-brand/30 transition hover:scale-105"
      >
        <MessageCircle className="h-6 w-6" />
        {unreadCount > 0 && (
          <span className="absolute -top-1 -right-1 flex min-h-5 min-w-5 items-center justify-center rounded-full bg-red-500 px-1 text-[10px] font-bold text-white">
            {unreadCount > 9 ? '9+' : unreadCount}
          </span>
        )}
      </button>

      <AnimatePresence>
        {open && (
          <motion.div
            initial={{ opacity: 0, y: 20, scale: 0.96 }}
            animate={{ opacity: 1, y: 0, scale: 1 }}
            exit={{ opacity: 0, y: 20, scale: 0.96 }}
            className="fixed right-5 bottom-24 z-50 flex h-[70vh] w-[min(380px,calc(100vw-2rem))] flex-col overflow-hidden rounded-3xl border border-border bg-surface shadow-2xl"
          >
            <div className="flex items-center justify-between border-b border-border bg-brand px-5 py-4 text-white">
              <div>
                <p className="font-display text-base font-bold">Hỗ trợ khách hàng</p>
                <p className="text-xs text-white/80">Nhắn tin với admin</p>
              </div>
              <button
                type="button"
                onClick={() => setOpen(false)}
                className="cursor-pointer rounded-full p-2 hover:bg-white/10"
              >
                <X className="h-4 w-4" />
              </button>
            </div>

            <div className="flex-1 overflow-y-auto bg-surface-alt/40 px-4 py-4">
              {loading ? (
                <div className="flex h-full items-center justify-center">
                  <Loader2 className="h-6 w-6 animate-spin text-text-muted" />
                </div>
              ) : messages.length === 0 ? (
                <div className="flex h-full flex-col items-center justify-center text-center text-text-muted">
                  <MessageCircle className="h-10 w-10 opacity-40" />
                  <p className="mt-3 text-sm">Bắt đầu cuộc trò chuyện với shop</p>
                </div>
              ) : (
                <div className="space-y-3">
                  {messages.map((item) => {
                    const mine = item.senderRole === 'USER';
                    return (
                      <div
                        key={item.id}
                        className={`flex ${mine ? 'justify-end' : 'justify-start'}`}
                      >
                        <div
                          className={`${bubbleBase} ${
                            mine
                              ? 'bg-brand text-white'
                              : 'border border-border bg-white text-text-primary'
                          }`}
                        >
                          {!mine && (
                            <p className="mb-1 text-[11px] font-semibold text-text-muted">
                              {item.senderName}
                            </p>
                          )}
                          {item.type === 'IMAGE' ? (
                            <button
                              type="button"
                              onClick={() => setPreviewImage(item.content)}
                              className="block cursor-zoom-in"
                            >
                              <img
                                src={item.content}
                                alt="Chat attachment"
                                className="max-h-56 rounded-xl object-cover"
                              />
                            </button>
                          ) : (
                            <p>{item.content}</p>
                          )}
                          <p
                            className={`mt-1 text-[10px] ${
                              mine ? 'text-white/70' : 'text-text-muted'
                            }`}
                          >
                            {new Date(item.createdAt).toLocaleTimeString('vi-VN', {
                              hour: '2-digit',
                              minute: '2-digit',
                            })}
                            {mine ? ` · ${item.readByAdmin ? 'Đã xem' : 'Đã gửi'}` : ''}
                          </p>
                        </div>
                      </div>
                    );
                  })}
                  <div ref={bottomRef} />
                </div>
              )}
            </div>

            <div className="border-t border-border bg-surface px-3 py-3">
              <div className="flex items-end gap-2">
                <label className="flex h-11 w-11 cursor-pointer items-center justify-center rounded-2xl border border-border bg-surface-alt text-text-secondary hover:text-brand">
                  {uploading ? (
                    <Loader2 className="h-4 w-4 animate-spin" />
                  ) : (
                    <ImagePlus className="h-4 w-4" />
                  )}
                  <input
                    type="file"
                    accept="image/jpeg,image/png,image/webp,image/gif"
                    className="hidden"
                    disabled={uploading}
                    onChange={(e) => {
                      const file = e.target.files?.[0];
                      if (file) {
                        void handleUploadImage(file);
                      }
                      e.target.value = '';
                    }}
                  />
                </label>
                <textarea
                  rows={1}
                  value={message}
                  onChange={(e) => setMessage(e.target.value)}
                  onKeyDown={(e) => {
                    if (e.key === 'Enter' && !e.shiftKey) {
                      e.preventDefault();
                      void sendTextMessage();
                    }
                  }}
                  placeholder="Nhập tin nhắn..."
                  className="min-h-11 flex-1 resize-none rounded-2xl border border-border bg-surface-alt px-4 py-3 text-sm text-text-primary outline-none focus:border-brand"
                />
                <button
                  type="button"
                  onClick={() => void sendTextMessage()}
                  disabled={sending || !message.trim()}
                  className="flex h-11 w-11 cursor-pointer items-center justify-center rounded-2xl bg-brand text-white disabled:cursor-not-allowed disabled:opacity-60"
                >
                  {sending ? (
                    <Loader2 className="h-4 w-4 animate-spin" />
                  ) : (
                    <SendHorizontal className="h-4 w-4" />
                  )}
                </button>
              </div>
              {conversation && (
                <p className="mt-2 px-1 text-[11px] text-text-muted">
                  Hội thoại #{conversation.id.slice(-6).toUpperCase()}
                </p>
              )}
            </div>
          </motion.div>
        )}
      </AnimatePresence>
      <AnimatePresence>
        {previewImage && (
          <motion.button
            type="button"
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            onClick={() => setPreviewImage(null)}
            className="fixed inset-0 z-[60] flex cursor-zoom-out items-center justify-center bg-black/80 p-4"
          >
            <img
              src={previewImage}
              alt="Preview"
              className="max-h-[90vh] max-w-[90vw] rounded-2xl object-contain"
            />
          </motion.button>
        )}
      </AnimatePresence>
    </>
  );
}
