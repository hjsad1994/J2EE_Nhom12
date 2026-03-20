import { useEffect, useRef } from 'react';
import { Client } from '@stomp/stompjs';
import { useAuthStore } from '@/store/useAuthStore';
import { useToastStore } from '@/store/useToastStore';

/**
 * Listens for real-time ban/unban events via WebSocket.
 * When the current user is banned by an admin, logs them out and redirects to login.
 * When unbanned, shows an informational toast.
 */
export function useBanWebSocket() {
  const { token, user, logout } = useAuthStore();
  const clientRef = useRef<Client | null>(null);

  useEffect(() => {
    if (!token || !user) return;

    const backendUrl =
      import.meta.env.VITE_BACKEND_URL ??
      import.meta.env.VITE_API_BASE_URL?.replace(/\/api$/, '') ??
      'http://localhost:8080';
    const brokerURL = backendUrl.replace(/^http/, 'ws') + '/ws';

    const client = new Client({
      brokerURL,
      connectHeaders: {
        Authorization: `Bearer ${token}`,
      },
      reconnectDelay: 5000,
      onConnect: () => {
        client.subscribe(`/user/queue/ban-status`, (message) => {
          const data = JSON.parse(message.body) as {
            banned: boolean;
            message: string;
          };

          if (data.banned) {
            // Show warning toast then force logout
            useToastStore.getState().addToast('warning', data.message);
            setTimeout(() => {
              logout();
              window.location.href = '/login';
            }, 2000);
          } else {
            // Unbanned — just inform the user (they'll need to log in again)
            useToastStore.getState().addToast('info', data.message);
          }
        });
      },
    });

    client.activate();
    clientRef.current = client;

    return () => {
      client.deactivate();
      clientRef.current = null;
    };
  }, [token, user, logout]);
}
