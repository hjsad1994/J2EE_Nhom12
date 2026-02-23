import { Heart, Menu, ShoppingCart, Smartphone, User, X } from 'lucide-react';
import { AnimatePresence, motion } from 'motion/react';
import { useState } from 'react';
import { Link } from 'react-router';

import { useWishlistStore } from '@/store/useWishlistStore';

// TODO: Replace with real auth state from Zustand store
const isLoggedIn = false;

const navLinks = [
  { label: 'Trang chủ', href: '/' },
  { label: 'Sản phẩm', href: '/products' },
];

export default function Navbar() {
  const [mobileOpen, setMobileOpen] = useState(false);
  const wishlistCount = useWishlistStore((s) => s.items.length);

  return (
    <motion.header
      initial={{ y: -100 }}
      animate={{ y: 0 }}
      transition={{ type: 'spring', stiffness: 100, damping: 20 }}
      className="fixed top-0 right-0 left-0 z-50 border-b border-border bg-surface/80 backdrop-blur-xl"
    >
      <nav className="mx-auto flex max-w-7xl items-center justify-between px-6 py-4">
        {/* Logo */}
        <Link to="/" className="flex items-center gap-2.5 no-underline">
          <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-brand">
            <Smartphone className="h-5 w-5 text-white" />
          </div>
          <span className="font-display text-xl font-bold tracking-tight text-brand">
            NEBULA
          </span>
        </Link>

        {/* Desktop nav */}
        <ul className="hidden items-center gap-8 md:flex">
          {navLinks.map((link) => (
            <li key={link.href}>
              <Link
                to={link.href}
                className="text-sm font-medium text-text-secondary transition-colors duration-200 hover:text-brand no-underline"
              >
                {link.label}
              </Link>
            </li>
          ))}
        </ul>

        {/* Actions */}
        <div className="flex items-center gap-3">
          {/* Wishlist */}
          <Link
            to="/wishlist"
            className="relative rounded-full p-2 text-text-secondary transition-colors hover:bg-surface-alt hover:text-brand"
            title="Yêu thích"
          >
            <Heart className="h-5 w-5" />
            <AnimatePresence>
              {wishlistCount > 0 && (
                <motion.span
                  initial={{ scale: 0 }}
                  animate={{ scale: 1 }}
                  exit={{ scale: 0 }}
                  className="absolute -top-0.5 -right-0.5 flex h-4 w-4 items-center justify-center rounded-full bg-brand-accent text-[10px] font-bold text-white ring-2 ring-surface"
                >
                  {wishlistCount}
                </motion.span>
              )}
            </AnimatePresence>
          </Link>

          {/* Cart */}
          <button
            type="button"
            className="relative cursor-pointer rounded-full p-2 text-text-secondary transition-colors hover:bg-surface-alt hover:text-brand"
          >
            <ShoppingCart className="h-5 w-5" />
            <span className="absolute -top-0.5 -right-0.5 flex h-4 w-4 items-center justify-center rounded-full bg-brand text-[10px] font-bold text-white">
              3
            </span>
          </button>

          {/* Profile / Login */}
          {isLoggedIn ? (
            <Link
              to="/profile"
              className="rounded-full p-2 text-text-secondary transition-colors hover:bg-surface-alt hover:text-brand"
              title="Tài khoản"
            >
              <User className="h-5 w-5" />
            </Link>
          ) : (
            <Link
              to="/login"
              className="hidden rounded-lg bg-brand px-4 py-2 text-sm font-medium text-white transition-colors hover:bg-brand-accent no-underline md:block"
            >
              Đăng nhập
            </Link>
          )}

          {/* Mobile toggle */}
          <button
            type="button"
            onClick={() => setMobileOpen(!mobileOpen)}
            className="cursor-pointer rounded-lg p-2 text-text-secondary transition-colors hover:bg-surface-alt hover:text-brand md:hidden"
          >
            {mobileOpen ? (
              <X className="h-5 w-5" />
            ) : (
              <Menu className="h-5 w-5" />
            )}
          </button>
        </div>
      </nav>

      {/* Mobile menu */}
      <AnimatePresence>
        {mobileOpen && (
          <motion.div
            initial={{ opacity: 0, height: 0 }}
            animate={{ opacity: 1, height: 'auto' }}
            exit={{ opacity: 0, height: 0 }}
            className="overflow-hidden border-t border-border md:hidden"
          >
            <ul className="flex flex-col gap-1 px-6 py-4">
              {navLinks.map((link) => (
                <li key={link.href}>
                  <Link
                    to={link.href}
                    onClick={() => setMobileOpen(false)}
                    className="block rounded-lg px-4 py-2.5 text-sm font-medium text-text-secondary transition-colors hover:bg-surface-alt hover:text-brand no-underline"
                  >
                    {link.label}
                  </Link>
                </li>
              ))}
              {!isLoggedIn && (
                <li>
                  <Link
                    to="/login"
                    onClick={() => setMobileOpen(false)}
                    className="block rounded-lg px-4 py-2.5 text-sm font-medium text-brand transition-colors hover:bg-surface-alt no-underline"
                  >
                    Đăng nhập
                  </Link>
                </li>
              )}
            </ul>
          </motion.div>
        )}
      </AnimatePresence>
    </motion.header>
  );
}
