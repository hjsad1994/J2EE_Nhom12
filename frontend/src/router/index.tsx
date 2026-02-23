import { createBrowserRouter } from 'react-router';
import App from '@/App';

/**
 * Application router configuration.
 *
 * Add routes here as features are implemented:
 *
 * Example:
 *   {
 *     path: '/dashboard',
 *     lazy: () => import('@/pages/Dashboard'),
 *   }
 */
export const router = createBrowserRouter([
  {
    path: '/',
    Component: App,
    children: [
      {
        index: true,
        lazy: () => import('@/pages/Home'),
      },
    ],
  },
]);
