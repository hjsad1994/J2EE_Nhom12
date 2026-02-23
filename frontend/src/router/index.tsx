import { createBrowserRouter } from 'react-router';
import App from '@/App';

export const router = createBrowserRouter([
  {
    path: '/',
    Component: App,
    children: [
      {
        index: true,
        lazy: () => import('@/pages/Home'),
      },
      {
        path: 'products',
        lazy: () => import('@/pages/Products'),
      },
      {
        path: 'products/:id',
        lazy: () => import('@/pages/ProductDetail'),
      },
      {
        path: 'login',
        lazy: () => import('@/pages/Auth'),
      },
      {
        path: 'about',
        lazy: () => import('@/pages/About'),
      },
      {
        path: 'wishlist',
        lazy: () => import('@/pages/Wishlist'),
      },
      {
        path: 'cart',
        lazy: () => import('@/pages/Cart'),
      },
    ],
  },
]);
