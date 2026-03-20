import { useOrderWebSocket } from '@/hooks/useOrderWebSocket';
import { useRoleWebSocket } from '@/hooks/useRoleWebSocket';
import { useBanWebSocket } from '@/hooks/useBanWebSocket';
import { useChatSummaryWebSocket } from '@/hooks/useChatSummaryWebSocket';

export default function WebSocketListeners() {
  useRoleWebSocket();
  useOrderWebSocket();
  useBanWebSocket();
  useChatSummaryWebSocket();
  return null;
}
