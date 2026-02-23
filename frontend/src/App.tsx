import { Outlet } from 'react-router';

/**
 * Root layout component.
 * Wraps all routes with shared layout (header, sidebar, footer, etc.)
 */
export default function App() {
  return (
    <div>
      <Outlet />
    </div>
  );
}
