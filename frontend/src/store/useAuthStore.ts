import { create } from 'zustand';
import { persist } from 'zustand/middleware';

import { useCartStore } from '@/store/useCartStore';

export interface AuthUser {
  id: string;
  username: string;
  email: string;
  role: 'USER' | 'ADMIN';
}

interface AuthState {
  token: string | null;
  user: AuthUser | null;
  isLoggedIn: boolean;
  isAdmin: boolean;
  login: (token: string, user: AuthUser) => void;
  logout: () => void;
  syncRole: (role: 'USER' | 'ADMIN') => void;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      token: null,
      user: null,
      isLoggedIn: false,
      isAdmin: false,

      login: (token, user) => {
        // Clear cart if a different user is logging in (e.g., user switches account)
        const currentUser = get().user;
        if (currentUser && currentUser.id !== user.id) {
          useCartStore.getState().clear();
        }
        set({ token, user, isLoggedIn: true, isAdmin: user.role === 'ADMIN' });
      },

      logout: () => {
        useCartStore.getState().clear();
        set({ token: null, user: null, isLoggedIn: false, isAdmin: false });
      },

      syncRole: (role) =>
        set((state) => ({
          user: state.user ? { ...state.user, role } : null,
          isAdmin: role === 'ADMIN',
        })),
    }),
    { name: 'nebula-auth', version: 1 },
  ),
);
