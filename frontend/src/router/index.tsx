import { createBrowserRouter } from 'react-router';
import App from '@/App';
import AdminRoute from '@/components/routes/AdminRoute';
import PrivateRoute from '@/components/routes/PrivateRoute';

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
      {
        path: 'checkout',
        lazy: () => import('@/pages/Checkout'),
      },
      {
        path: 'checkout/success',
        lazy: () => import('@/pages/CheckoutSuccess'),
      },
      {
        path: 'checkout/result',
        lazy: () => import('@/pages/CheckoutResult'),
      },
      {
        path: 'oauth2/callback',
        lazy: () => import('@/pages/OAuth2Callback'),
      },
      {
        path: 'profile',
        Component: PrivateRoute,
        children: [
          {
            index: true,
            lazy: () => import('@/pages/Profile'),
          },
        ],
      },
    ],
  },
  {
    path: '/admin',
    Component: AdminRoute,
    children: [
      {
        index: true,
        lazy: () => import('@/pages/admin/Dashboard'),
      },
    ],
  },
]);
