import { createBrowserRouter } from "react-router";

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
		path: "/",
		children: [
			{
				index: true,
				lazy: () => import("@/pages/Home"),
			},
		],
	},
]);
