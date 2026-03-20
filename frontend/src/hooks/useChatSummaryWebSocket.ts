import { Client } from '@stomp/stompjs';
import { useEffect, useRef } from 'react';

import apiClient from '@/api/client';
import type { ApiResponse } from '@/api/types';
import { getBrokerUrl } from '@/lib/websocket';
import { useAuthStore } from '@/store/useAuthStore';
import { useChatStore } from '@/store/useChatStore';
import type { ChatConversation } from '@/types/chat';

interface ChatUnreadSummaryResponse {
  unreadCountForUser: number;
  unreadCountForAdmin: number;
}

export function useChatSummaryWebSocket() {
  const { token, isLoggedIn, isAdmin } = useAuthStore();
  const setUserUnreadCount = useChatStore((s) => s.setUserUnreadCount);
  const setAdminUnreadCount = useChatStore((s) => s.setAdminUnreadCount);
  const clientRef = useRef<Client | null>(null);

  useEffect(() => {
    if (!isLoggedIn || !token) {
      setUserUnreadCount(0);
      setAdminUnreadCount(0);
      return;
    }

    const fetchSummary = async () => {
      const endpoint = isAdmin
        ? '/chat/admin/unread-summary'
        : '/chat/my/unread-summary';
      try {
        const res = await apiClient.get<ApiResponse<ChatUnreadSummaryResponse>>(
          endpoint,
        );
        setUserUnreadCount(res.data.data.unreadCountForUser);
        setAdminUnreadCount(res.data.data.unreadCountForAdmin);
      } catch {
        setUserUnreadCount(0);
        setAdminUnreadCount(0);
      }
    };

    void fetchSummary();

    const client = new Client({
      brokerURL: getBrokerUrl(),
      connectHeaders: {
        Authorization: `Bearer ${token}`,
      },
      reconnectDelay: 5000,
      onConnect: () => {
        if (isAdmin) {
          client.subscribe('/topic/admin/chat/conversations', () => {
            void fetchSummary();
          });
        } else {
          client.subscribe('/user/queue/chat/conversation', (frame) => {
            const nextConversation = JSON.parse(frame.body) as ChatConversation;
            setUserUnreadCount(nextConversation.unreadCountForUser ?? 0);
          });
        }
      },
    });

    client.activate();
    clientRef.current = client;

    return () => {
      client.deactivate();
      clientRef.current = null;
    };
  }, [
    isAdmin,
    isLoggedIn,
    setAdminUnreadCount,
    setUserUnreadCount,
    token,
  ]);
}
