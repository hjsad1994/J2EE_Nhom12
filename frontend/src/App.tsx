import { Outlet } from 'react-router';
import Footer from '@/components/layout/Footer';
import Navbar from '@/components/layout/Navbar';

/**
 * Root layout component.
 * Wraps all routes with shared layout (Navbar + Footer).
 */
export default function App() {
  return (
    <>
      <Navbar />
      <main className="flex-1">
        <Outlet />
      </main>
      <Footer />
    </>
  );
}
