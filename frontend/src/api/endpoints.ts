/**
 * API endpoint constants.
 * Organize by domain/feature for easy discovery.
 */
export const ENDPOINTS = {
  AUTH: {
    LOGIN: '/auth/login',
    REGISTER: '/auth/register',
  },
  PRODUCTS: {
    BASE: '/products',
    BY_ID: (id: string) => `/products/${id}`,
  },
  CATEGORIES: {
    BASE: '/categories',
    BY_ID: (id: string) => `/categories/${id}`,
  },
  ORDERS: {
    BASE: '/orders',
    MY: '/orders/my',
    BY_ID: (id: string) => `/orders/${id}`,
    STATUS: (id: string) => `/orders/${id}/status`,
  },
  MOMO: {
    CREATE: (orderId: string) => `/momo/create?orderId=${orderId}`,
  },
  WISHLIST: {
    BASE: '/wishlist',
    TOGGLE: (productId: string) => `/wishlist/${productId}`,
  },
  REVIEWS: {
    BASE: '/reviews',
    BY_ID: (id: string) => `/reviews/${id}`,
  },
  USERS: {
    ME: '/users/me',
    CHANGE_PASSWORD: '/users/me/password',
  },
} as const;
