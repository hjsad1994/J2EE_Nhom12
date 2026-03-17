import { useOrderWebSocket } from '@/hooks/useOrderWebSocket';
import { useRoleWebSocket } from '@/hooks/useRoleWebSocket';
import { useBanWebSocket } from '@/hooks/useBanWebSocket';

export default function WebSocketListeners() {
  useRoleWebSocket();
  useOrderWebSocket();
  useBanWebSocket();
  return null;
}
