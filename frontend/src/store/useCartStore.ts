import { create } from 'zustand';
import { persist } from 'zustand/middleware';

import apiClient from '@/api/client';
import { ENDPOINTS } from '@/api/endpoints';
import { useAuthStore } from '@/store/useAuthStore';
import type { ApiResponse } from '@/api/types';
import type { Product } from '@/types/product';

export const MAX_QUANTITY = 99;

export interface CartItem {
  product: Product;
  quantity: number;
}

/** Shape returned by /api/cart */
interface ServerCartItem {
  productId: string;
  productName: string;
  productImage: string;
  brand: string;
  price: number;
  quantity: number;
}

interface ServerCartResponse {
  id: string;
  userId: string;
  items: ServerCartItem[];
  updatedAt: string;
}

interface CartState {
  items: CartItem[];
  isLoading: boolean;

  addItem: (product: Product) => Promise<void>;
  removeItem: (productId: string) => void;
  updateQuantity: (productId: string, quantity: number) => void;
  clear: () => void;

  /** Fetch cart from server (called on login). */
  fetchCart: () => Promise<void>;
  /**
   * Merge guest cart (current local items) with server cart on login.
   * Called right after login() so that items added while browsing as guest are preserved.
   */
  mergeOnLogin: () => Promise<void>;

  totalItems: () => number;
  totalPrice: () => number;
}

/** Convert a server cart item to the frontend CartItem format. */
const fromServer = (item: ServerCartItem): CartItem => ({
  product: {
    id: item.productId,
    name: item.productName,
    image: item.productImage,
    brand: item.brand,
    price: item.price,
    // Fields not stored in cart snapshot — use safe defaults
    rating: 0,
    stock: 99,
  },
  quantity: item.quantity,
});

const isLoggedIn = () => useAuthStore.getState().isLoggedIn;

export const useCartStore = create<CartState>()(
  persist(
    (set, get) => ({
      items: [],
      isLoading: false,

      addItem: async (product) => {
        // 1. Optimistic local update
        const prev = get().items;
        const existing = prev.find((i) => i.product.id === product.id);
        if (existing) {
          if (existing.quantity >= MAX_QUANTITY) return;
          set({
            items: prev.map((i) =>
              i.product.id === product.id
                ? { ...i, quantity: Math.min(i.quantity + 1, MAX_QUANTITY) }
                : i,
            ),
          });
        } else {
          set({ items: [...prev, { product, quantity: 1 }] });
        }

        // 2. Sync to server if logged in
        if (!isLoggedIn()) return;
        try {
          const res = await apiClient.post<ApiResponse<ServerCartResponse>>(
            ENDPOINTS.CART.ITEMS,
            {
              productId: product.id,
              quantity: 1,
            },
          );
          set({ items: res.data.data.items.map(fromServer) });
        } catch {
          // Revert on error
          set({ items: prev });
        }
      },

      removeItem: (productId) => {
        const prev = get().items;
        set({ items: prev.filter((i) => i.product.id !== productId) });

        if (!isLoggedIn()) return;
        apiClient
          .delete<ApiResponse<ServerCartResponse>>(
            ENDPOINTS.CART.ITEM(productId),
          )
          .then((res) => set({ items: res.data.data.items.map(fromServer) }))
          .catch(() => set({ items: prev }));
      },

      updateQuantity: (productId, quantity) => {
        const prev = get().items;

        if (quantity <= 0) {
          set({ items: prev.filter((i) => i.product.id !== productId) });
        } else {
          const clamped = Math.min(quantity, MAX_QUANTITY);
          set({
            items: prev.map((i) =>
              i.product.id === productId ? { ...i, quantity: clamped } : i,
            ),
          });
        }

        if (!isLoggedIn()) return;
        if (quantity <= 0) {
          apiClient
            .delete<ApiResponse<ServerCartResponse>>(
              ENDPOINTS.CART.ITEM(productId),
            )
            .then((res) => set({ items: res.data.data.items.map(fromServer) }))
            .catch(() => set({ items: prev }));
        } else {
          apiClient
            .put<ApiResponse<ServerCartResponse>>(
              `${ENDPOINTS.CART.ITEM(productId)}?quantity=${Math.min(quantity, MAX_QUANTITY)}`,
            )
            .then((res) => set({ items: res.data.data.items.map(fromServer) }))
            .catch(() => set({ items: prev }));
        }
      },

      clear: () => {
        set({ items: [] });
        if (!isLoggedIn()) return;
        apiClient.delete(ENDPOINTS.CART.BASE).catch(() => {});
      },

      fetchCart: async () => {
        if (!isLoggedIn()) return;
        set({ isLoading: true });
        try {
          const res = await apiClient.get<ApiResponse<ServerCartResponse>>(
            ENDPOINTS.CART.BASE,
          );
          set({ items: res.data.data.items.map(fromServer) });
        } catch {
          // Silent fail — local state is fallback
        } finally {
          set({ isLoading: false });
        }
      },

      mergeOnLogin: async () => {
        const localItems = get().items;

        // No local items — just fetch server cart
        if (localItems.length === 0) {
          await get().fetchCart();
          return;
        }

        // Send local items to server for merge, get back merged result
        try {
          const payload = {
            items: localItems.map((i) => ({
              productId: i.product.id,
              quantity: i.quantity,
            })),
          };
          const res = await apiClient.post<ApiResponse<ServerCartResponse>>(
            ENDPOINTS.CART.SYNC,
            payload,
          );
          set({ items: res.data.data.items.map(fromServer) });
        } catch {
          // If sync fails, fall back to fetching server cart
          await get().fetchCart();
        }
      },

      totalItems: () => get().items.reduce((sum, i) => sum + i.quantity, 0),
      totalPrice: () =>
        get().items.reduce((sum, i) => sum + i.product.price * i.quantity, 0),
    }),
    {
      name: 'nebula-cart',
      version: 2,
      // Only persist items for guest browsing. When logged in, server is source of truth.
      partialize: (state) => ({ items: state.items }),
    },
  ),
);
