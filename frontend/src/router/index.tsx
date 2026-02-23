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
    ],
  },
]);
