import { create } from 'zustand';

interface ChatState {
  userUnreadCount: number;
  adminUnreadCount: number;
  setUserUnreadCount: (count: number) => void;
  setAdminUnreadCount: (count: number) => void;
}

export const useChatStore = create<ChatState>((set) => ({
  userUnreadCount: 0,
  adminUnreadCount: 0,
  setUserUnreadCount: (count) => set({ userUnreadCount: count }),
  setAdminUnreadCount: (count) => set({ adminUnreadCount: count }),
}));
