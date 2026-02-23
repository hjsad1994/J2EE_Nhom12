import {
  Facebook,
  Github,
  Mail,
  MapPin,
  Phone,
  Smartphone,
  Twitter,
} from 'lucide-react';
import { Link } from 'react-router';

const footerLinks = {
  products: [
    { label: 'iPhone 16 Pro Max', href: '/products/iphone-16-pro-max' },
    {
      label: 'Samsung Galaxy S25 Ultra',
      href: '/products/samsung-galaxy-s25-ultra',
    },
    { label: 'Google Pixel 9 Pro', href: '/products/google-pixel-9-pro' },
    { label: 'Tất cả sản phẩm', href: '/products' },
  ],
  company: [
    { label: 'Về chúng tôi', href: '/' },
    { label: 'Liên hệ', href: '/' },
    { label: 'Tuyển dụng', href: '/' },
    { label: 'Blog', href: '/' },
  ],
  support: [
    { label: 'Trung tâm hỗ trợ', href: '/' },
    { label: 'Chính sách bảo hành', href: '/' },
    { label: 'Đổi trả', href: '/' },
    { label: 'FAQ', href: '/' },
  ],
};

export default function Footer() {
  return (
    <footer className="mt-auto border-t border-border bg-surface-alt">
      <div className="mx-auto max-w-7xl px-6 py-16">
        {/* Top section */}
        <div className="grid grid-cols-1 gap-12 md:grid-cols-2 lg:grid-cols-5">
          {/* Brand */}
          <div className="lg:col-span-2">
            <Link
              to="/"
              className="mb-4 flex items-center gap-2.5 no-underline"
            >
              <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-brand">
                <Smartphone className="h-5 w-5 text-white" />
              </div>
              <span className="font-display text-xl font-bold tracking-tight text-brand">
                NEBULA
              </span>
            </Link>
            <p className="mb-6 max-w-sm text-sm leading-relaxed text-text-secondary">
              Khám phá thế giới công nghệ đỉnh cao. Chúng tôi mang đến những
              chiếc điện thoại hàng đầu với trải nghiệm mua sắm tuyệt vời nhất.
            </p>
            <div className="flex gap-3">
              {[Github, Facebook, Twitter].map((Icon, i) => (
                <a
                  key={i}
                  href="#"
                  className="flex h-9 w-9 items-center justify-center rounded-full border border-border text-text-muted transition-all hover:border-brand hover:text-brand"
                >
                  <Icon className="h-4 w-4" />
                </a>
              ))}
            </div>
          </div>

          {/* Links */}
          {Object.entries(footerLinks).map(([title, links]) => (
            <div key={title}>
              <h4 className="mb-4 font-display text-sm font-semibold uppercase tracking-wider text-brand">
                {title === 'products'
                  ? 'Sản phẩm'
                  : title === 'company'
                    ? 'Công ty'
                    : 'Hỗ trợ'}
              </h4>
              <ul className="space-y-2.5">
                {links.map((link) => (
                  <li key={link.href}>
                    <Link
                      to={link.href}
                      className="text-sm text-text-secondary transition-colors hover:text-brand no-underline"
                    >
                      {link.label}
                    </Link>
                  </li>
                ))}
              </ul>
            </div>
          ))}
        </div>

        {/* Contact info */}
        <div className="mt-12 flex flex-wrap gap-6 border-t border-border pt-8 text-sm text-text-muted">
          <span className="flex items-center gap-2">
            <MapPin className="h-4 w-4" /> 123 Nguyễn Huệ, Q.1, TP.HCM
          </span>
          <span className="flex items-center gap-2">
            <Phone className="h-4 w-4" /> 1900 1234
          </span>
          <span className="flex items-center gap-2">
            <Mail className="h-4 w-4" /> contact@nebula.vn
          </span>
        </div>

        {/* Bottom */}
        <div className="mt-8 flex flex-col items-center justify-between gap-4 border-t border-border pt-8 text-sm text-text-muted md:flex-row">
          <p>
            &copy; {new Date().getFullYear()} Nebula Tech. All rights reserved.
          </p>
          <p className="text-xs">Nhóm 12 - Đồ án cuối kỳ Java</p>
        </div>
      </div>
    </footer>
  );
}
