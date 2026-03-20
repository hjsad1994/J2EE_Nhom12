import { Client } from '@stomp/stompjs';
import {
  ImagePlus,
  Loader2,
  MessageCircle,
  Search,
  SendHorizontal,
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

export default function AdminChatPanel() {
  const { token } = useAuthStore();
  const addToast = useToastStore((s) => s.addToast);
  const setAdminUnreadCount = useChatStore((s) => s.setAdminUnreadCount);
  const [loading, setLoading] = useState(true);
  const [messagesLoading, setMessagesLoading] = useState(false);
  const [sending, setSending] = useState(false);
  const [uploading, setUploading] = useState(false);
  const [query, setQuery] = useState('');
  const [message, setMessage] = useState('');
  const [conversations, setConversations] = useState<ChatConversation[]>([]);
  const [selectedId, setSelectedId] = useState<string | null>(null);
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [previewImage, setPreviewImage] = useState<string | null>(null);
  const clientRef = useRef<Client | null>(null);
  const bottomRef = useRef<HTMLDivElement | null>(null);

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth', block: 'end' });
  }, [messages]);

  useEffect(() => {
    const fetchConversations = async () => {
      setLoading(true);
      try {
        const res = await apiClient.get<ApiResponse<ChatConversation[]>>(
          ENDPOINTS.CHAT.ADMIN_CONVERSATIONS,
        );
        setConversations(res.data.data);
        setAdminUnreadCount(
          res.data.data.reduce(
            (total, item) => total + item.unreadCountForAdmin,
            0,
          ),
        );
        setSelectedId((prev) => prev ?? res.data.data[0]?.id ?? null);
      } catch {
        addToast('error', 'Không thể tải danh sách chat');
      } finally {
        setLoading(false);
      }
    };

    void fetchConversations();
  }, [addToast, setAdminUnreadCount]);

  useEffect(() => {
    if (!selectedId) {
      setMessages([]);
      return;
    }

    const fetchMessages = async () => {
      setMessagesLoading(true);
      try {
        const res = await apiClient.get<ApiResponse<ChatMessage[]>>(
          ENDPOINTS.CHAT.ADMIN_MESSAGES(selectedId),
        );
        setMessages(res.data.data);
        setConversations((prev) => {
          const next = prev.map((item) =>
            item.id === selectedId ? { ...item, unreadCountForAdmin: 0 } : item,
          );
          setAdminUnreadCount(
            next.reduce((total, item) => total + item.unreadCountForAdmin, 0),
          );
          return next;
        });
      } catch {
        addToast('error', 'Không thể tải tin nhắn');
      } finally {
        setMessagesLoading(false);
      }
    };

    void fetchMessages();
  }, [addToast, selectedId, setAdminUnreadCount]);

  useEffect(() => {
    if (!token) return;

    const client = new Client({
      brokerURL: getBrokerUrl(),
      connectHeaders: {
        Authorization: `Bearer ${token}`,
      },
      reconnectDelay: 5000,
      onConnect: () => {
        client.subscribe('/topic/admin/chat/conversations', (frame) => {
          const nextConversation = JSON.parse(frame.body) as ChatConversation;
          setConversations((prev) => {
            const existed = prev.some((item) => item.id === nextConversation.id);
            const merged = prev.some((item) => item.id === nextConversation.id)
              ? prev.map((item) =>
                  item.id === nextConversation.id ? nextConversation : item,
                )
              : [nextConversation, ...prev];
            if (!existed && nextConversation.unreadCountForAdmin > 0) {
              setSelectedId(nextConversation.id);
            }
            setAdminUnreadCount(
              merged.reduce(
                (total, item) => total + item.unreadCountForAdmin,
                0,
              ),
            );
            return merged.sort(
              (a, b) =>
                new Date(b.updatedAt).getTime() - new Date(a.updatedAt).getTime(),
            );
          });
        });

        client.subscribe('/topic/admin/chat/messages', (frame) => {
          const nextMessage = JSON.parse(frame.body) as ChatMessage;
          setMessages((prev) =>
            selectedId === nextMessage.conversationId
              ? prev.some((item) => item.id === nextMessage.id)
                ? prev.map((item) =>
                    item.id === nextMessage.id ? nextMessage : item,
                  )
                : [...prev, nextMessage]
              : prev,
          );
        });
      },
    });

    client.activate();
    clientRef.current = client;

    return () => {
      client.deactivate();
      clientRef.current = null;
    };
  }, [selectedId, setAdminUnreadCount, token]);

  const selectedConversation = conversations.find((item) => item.id === selectedId) ?? null;

  const filteredConversations = useMemo(() => {
    const normalized = query.trim().toLowerCase();
    if (!normalized) return conversations;
    return conversations.filter(
      (item) =>
        item.username.toLowerCase().includes(normalized) ||
        item.userEmail.toLowerCase().includes(normalized),
    );
  }, [conversations, query]);

  const sendMessage = async (content: string, type: 'TEXT' | 'IMAGE') => {
    if (!selectedConversation) return;

    setSending(type === 'TEXT');
    try {
      const res = await apiClient.post<ApiResponse<ChatMessage>>(
        ENDPOINTS.CHAT.ADMIN_SEND,
        {
          conversationId: selectedConversation.id,
          type,
          content,
        },
      );
      setMessages((prev) =>
        prev.some((item) => item.id === res.data.data.id)
          ? prev
          : [...prev, res.data.data],
      );
      if (type === 'TEXT') {
        setMessage('');
      }
    } catch {
      addToast('error', 'Không thể gửi tin nhắn');
    } finally {
      setSending(false);
    }
  };

  const handleUploadImage = async (file: File) => {
    if (!selectedConversation) return;

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
      await sendMessage(uploadRes.data.data, 'IMAGE');
    } catch {
      addToast('error', 'Không thể gửi hình ảnh');
    } finally {
      setUploading(false);
    }
  };

  return (
    <div className="grid min-h-[72vh] gap-6 lg:grid-cols-[340px_1fr]">
      <div className="overflow-hidden rounded-2xl bg-white shadow-sm ring-1 ring-gray-100">
        <div className="border-b border-gray-100 px-5 py-4">
          <h2 className="font-bold text-gray-800">Hộp thư hỗ trợ</h2>
          <div className="mt-3 flex items-center gap-2 rounded-xl border border-gray-200 bg-gray-50 px-3 py-2.5">
            <Search className="h-4 w-4 text-gray-400" />
            <input
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              placeholder="Tìm theo tên hoặc email"
              className="w-full bg-transparent text-sm outline-none"
            />
          </div>
        </div>
        <div className="max-h-[calc(72vh-86px)] overflow-y-auto">
          {loading ? (
            <div className="flex items-center justify-center py-20">
              <Loader2 className="h-7 w-7 animate-spin text-purple-500" />
            </div>
          ) : filteredConversations.length === 0 ? (
            <div className="flex flex-col items-center justify-center py-20 text-gray-400">
              <MessageCircle className="h-10 w-10 text-gray-200" />
              <p className="mt-3 text-sm">Chưa có cuộc trò chuyện nào</p>
            </div>
          ) : (
            filteredConversations.map((item) => (
              <button
                key={item.id}
                type="button"
                onClick={() => setSelectedId(item.id)}
                className={`flex w-full cursor-pointer items-start gap-3 border-b border-gray-50 px-5 py-4 text-left transition hover:bg-gray-50 ${
                  selectedId === item.id ? 'bg-purple-50' : 'bg-white'
                }`}
              >
                <div className="flex h-10 w-10 items-center justify-center rounded-full bg-gradient-to-br from-purple-400 to-indigo-500 text-sm font-bold text-white">
                  {item.username.charAt(0).toUpperCase()}
                </div>
                <div className="min-w-0 flex-1">
                  <div className="flex items-center justify-between gap-3">
                    <p className="truncate font-semibold text-gray-800">
                      {item.username}
                    </p>
                    {item.unreadCountForAdmin > 0 && (
                      <span className="rounded-full bg-red-500 px-2 py-0.5 text-[11px] font-bold text-white">
                        {item.unreadCountForAdmin}
                      </span>
                    )}
                  </div>
                  <p className="truncate text-xs text-gray-400">{item.userEmail}</p>
                  <p className="mt-1 truncate text-sm text-gray-500">
                    {item.lastMessagePreview || 'Chưa có tin nhắn'}
                  </p>
                </div>
              </button>
            ))
          )}
        </div>
      </div>

      <div className="flex min-h-[72vh] flex-col overflow-hidden rounded-2xl bg-white shadow-sm ring-1 ring-gray-100">
        {selectedConversation ? (
          <>
            <div className="border-b border-gray-100 px-6 py-4">
              <p className="font-bold text-gray-800">{selectedConversation.username}</p>
              <p className="text-xs text-gray-400">{selectedConversation.userEmail}</p>
            </div>

            <div className="flex-1 overflow-y-auto bg-gray-50/70 px-6 py-5">
              {messagesLoading ? (
                <div className="flex h-full items-center justify-center">
                  <Loader2 className="h-7 w-7 animate-spin text-purple-500" />
                </div>
              ) : (
                <div className="space-y-3">
                  {messages.map((item) => {
                    const mine = item.senderRole === 'ADMIN';
                    return (
                      <div
                        key={item.id}
                        className={`flex ${mine ? 'justify-end' : 'justify-start'}`}
                      >
                        <div
                          className={`max-w-[75%] rounded-2xl px-4 py-3 text-sm shadow-sm ${
                            mine
                              ? 'bg-gradient-to-r from-purple-500 to-indigo-600 text-white'
                              : 'border border-gray-200 bg-white text-gray-800'
                          }`}
                        >
                          {!mine && (
                            <p className="mb-1 text-[11px] font-semibold text-gray-400">
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
                                className="max-h-64 rounded-xl object-cover"
                              />
                            </button>
                          ) : (
                            <p>{item.content}</p>
                          )}
                          <p
                            className={`mt-1 text-[10px] ${
                              mine ? 'text-white/70' : 'text-gray-400'
                            }`}
                          >
                            {new Date(item.createdAt).toLocaleString('vi-VN', {
                              hour: '2-digit',
                              minute: '2-digit',
                              day: '2-digit',
                              month: '2-digit',
                            })}
                            {mine ? ` · ${item.readByUser ? 'Đã xem' : 'Đã gửi'}` : ''}
                          </p>
                        </div>
                      </div>
                    );
                  })}
                  <div ref={bottomRef} />
                </div>
              )}
            </div>

            <div className="border-t border-gray-100 bg-white px-5 py-4">
              <div className="flex items-end gap-3">
                <label className="flex h-11 w-11 cursor-pointer items-center justify-center rounded-2xl border border-gray-200 bg-gray-50 text-gray-500 hover:text-purple-600">
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
                      if (message.trim()) {
                        void sendMessage(message.trim(), 'TEXT');
                      }
                    }
                  }}
                  placeholder="Nhập phản hồi cho khách hàng..."
                  className="min-h-11 flex-1 resize-none rounded-2xl border border-gray-200 bg-gray-50 px-4 py-3 text-sm outline-none focus:border-purple-400"
                />
                <button
                  type="button"
                  onClick={() => void sendMessage(message.trim(), 'TEXT')}
                  disabled={sending || !message.trim()}
                  className="flex h-11 w-11 cursor-pointer items-center justify-center rounded-2xl bg-gradient-to-r from-purple-500 to-indigo-600 text-white disabled:cursor-not-allowed disabled:opacity-60"
                >
                  {sending ? (
                    <Loader2 className="h-4 w-4 animate-spin" />
                  ) : (
                    <SendHorizontal className="h-4 w-4" />
                  )}
                </button>
              </div>
            </div>
          </>
        ) : (
          <div className="flex h-full flex-col items-center justify-center text-center text-gray-400">
            <MessageCircle className="h-12 w-12 text-gray-200" />
            <p className="mt-3 text-sm">Chọn một cuộc trò chuyện để bắt đầu</p>
          </div>
        )}
      </div>
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
    </div>
  );
}
