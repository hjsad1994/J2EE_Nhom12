import { create } from 'zustand';
import { persist } from 'zustand/middleware';

import apiClient from '@/api/client';
import { ENDPOINTS } from '@/api/endpoints';
import { normalizeVndAmount } from '@/lib/currency';
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
  color?: string;
  storage?: string;
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
  removeItem: (productId: string, color?: string, storage?: string) => void;
  updateQuantity: (
    productId: string,
    quantity: number,
    color?: string,
    storage?: string,
  ) => void;
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
const toCartItemKey = (product: Product) =>
  [product.id, product.selectedColor ?? '', product.selectedStorage ?? ''].join(
    '::',
  );

const buildCartItemUrl = (product: Product) => {
  const params = new URLSearchParams();
  if (product.selectedColor) {
    params.set('color', product.selectedColor);
  }
  if (product.selectedStorage) {
    params.set('storage', product.selectedStorage);
  }

  const query = params.toString();
  const base = ENDPOINTS.CART.ITEM(product.id);
  return query ? `${base}?${query}` : base;
};

const fromServer = (item: ServerCartItem): CartItem => ({
  product: {
    id: item.productId,
    name: item.productName,
    image: item.productImage,
    brand: item.brand,
    price: normalizeVndAmount(item.price),
    selectedColor: item.color,
    selectedStorage: item.storage,
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
        const normalizedProduct = {
          ...product,
          price: normalizeVndAmount(product.price),
        };

        // 1. Optimistic local update
        const prev = get().items;
        const key = toCartItemKey(normalizedProduct);
        const existing = prev.find((i) => toCartItemKey(i.product) === key);
        if (existing) {
          if (existing.quantity >= MAX_QUANTITY) return;
          set({
            items: prev.map((i) =>
              toCartItemKey(i.product) === key
                ? { ...i, quantity: Math.min(i.quantity + 1, MAX_QUANTITY) }
                : i,
            ),
          });
        } else {
          set({ items: [...prev, { product: normalizedProduct, quantity: 1 }] });
        }

        // 2. Sync to server if logged in
        if (!isLoggedIn()) return;
        try {
          const res = await apiClient.post<ApiResponse<ServerCartResponse>>(
            ENDPOINTS.CART.ITEMS,
            {
              productId: product.id,
              color: normalizedProduct.selectedColor,
              storage: normalizedProduct.selectedStorage,
              quantity: 1,
            },
          );
          set({ items: res.data.data.items.map(fromServer) });
        } catch {
          // Revert on error
          set({ items: prev });
        }
      },

      removeItem: (productId, color, storage) => {
        const prev = get().items;
        set({
          items: prev.filter(
            (i) =>
              !(
                i.product.id === productId &&
                (i.product.selectedColor ?? '') === (color ?? '') &&
                (i.product.selectedStorage ?? '') === (storage ?? '')
              ),
          ),
        });

        if (!isLoggedIn()) return;
        const url = buildCartItemUrl({
          id: productId,
          name: '',
          brand: '',
          price: 0,
          image: '',
          rating: 0,
          stock: 0,
          selectedColor: color,
          selectedStorage: storage,
        });
        apiClient
          .delete<ApiResponse<ServerCartResponse>>(url)
          .then((res) => set({ items: res.data.data.items.map(fromServer) }))
          .catch(() => set({ items: prev }));
      },

      updateQuantity: (productId, quantity, color, storage) => {
        const prev = get().items;

        if (quantity <= 0) {
          set({
            items: prev.filter(
              (i) =>
                !(
                  i.product.id === productId &&
                  (i.product.selectedColor ?? '') === (color ?? '') &&
                  (i.product.selectedStorage ?? '') === (storage ?? '')
                ),
            ),
          });
        } else {
          const clamped = Math.min(quantity, MAX_QUANTITY);
          set({
            items: prev.map((i) =>
              i.product.id === productId &&
                (i.product.selectedColor ?? '') === (color ?? '') &&
                (i.product.selectedStorage ?? '') === (storage ?? '')
                ? { ...i, quantity: clamped }
                : i,
            ),
          });
        }

        if (!isLoggedIn()) return;
        const url = buildCartItemUrl({
          id: productId,
          name: '',
          brand: '',
          price: 0,
          image: '',
          rating: 0,
          stock: 0,
          selectedColor: color,
          selectedStorage: storage,
        });
        if (quantity <= 0) {
          apiClient
            .delete<ApiResponse<ServerCartResponse>>(url)
            .then((res) => set({ items: res.data.data.items.map(fromServer) }))
            .catch(() => set({ items: prev }));
        } else {
          apiClient
            .put<ApiResponse<ServerCartResponse>>(
              `${url}${url.includes('?') ? '&' : '?'}quantity=${Math.min(quantity, MAX_QUANTITY)}`,
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
              color: i.product.selectedColor,
              storage: i.product.selectedStorage,
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
        get().items.reduce(
          (sum, i) => sum + normalizeVndAmount(i.product.price) * i.quantity,
          0,
        ),
    }),
    {
      name: 'nebula-cart',
      version: 2,
      // Only persist items for guest browsing. When logged in, server is source of truth.
      partialize: (state) => ({ items: state.items }),
    },
  ),
);
