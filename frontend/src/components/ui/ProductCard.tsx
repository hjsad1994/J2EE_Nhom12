import { ShoppingCart, Star } from 'lucide-react';
import { motion } from 'motion/react';
import { Link } from 'react-router';

import type { Product } from '@/types/product';

interface ProductCardProps {
  product: Product;
  index?: number;
}

export default function ProductCard({ product, index = 0 }: ProductCardProps) {
  const discount = product.originalPrice
    ? Math.round((1 - product.price / product.originalPrice) * 100)
    : null;

  return (
    <motion.div
      initial={{ opacity: 0, y: 30 }}
      whileInView={{ opacity: 1, y: 0 }}
      viewport={{ once: true, margin: '-50px' }}
      transition={{ duration: 0.5, delay: Math.min(index, 4) * 0.1 }}
    >
      <Link
        to={`/products/${product.id}`}
        className="group relative block overflow-hidden rounded-2xl card card-hover no-underline"
      >
        {/* Badge */}
        {product.badge && (
          <span className="absolute top-3 left-3 z-10 rounded-full bg-brand px-3 py-1 text-xs font-semibold text-white">
            {product.badge}
          </span>
        )}

        {/* Discount */}
        {discount && (
          <span className="absolute top-3 right-3 z-10 rounded-full bg-red-500 px-2.5 py-1 text-xs font-bold text-white">
            -{discount}%
          </span>
        )}

        {/* Image */}
        <div className="relative flex items-center justify-center overflow-hidden bg-surface-alt p-6 pt-8">
          <motion.img
            src={product.image}
            alt={product.name}
            className="h-48 w-auto object-contain transition-transform duration-500 group-hover:scale-110"
            whileHover={{ rotateY: 8 }}
          />
        </div>

        {/* Info */}
        <div className="p-5">
          <p className="text-xs font-medium uppercase tracking-wider text-text-muted">
            {product.brand}
          </p>
          <h3 className="mt-1 font-display text-base font-semibold text-text-primary line-clamp-1">
            {product.name}
          </h3>
          {product.specs && (
            <p className="mt-1 text-xs text-text-secondary line-clamp-1">
              {product.specs}
            </p>
          )}

          {/* Rating */}
          <div className="mt-2 flex items-center gap-1">
            {Array.from({ length: 5 }).map((_, i) => (
              <Star
                key={i}
                className={`h-3.5 w-3.5 ${
                  i < Math.floor(product.rating)
                    ? 'fill-amber-400 text-amber-400'
                    : 'fill-transparent text-text-muted'
                }`}
              />
            ))}
            <span className="ml-1 text-xs text-text-muted">
              {product.rating.toFixed(1)}
            </span>
          </div>

          {/* Price + CTA */}
          <div className="mt-3 flex items-end justify-between">
            <div>
              <span className="font-display text-lg font-bold text-brand">
                {product.price.toLocaleString('vi-VN')}₫
              </span>
              {product.originalPrice && (
                <span className="ml-2 text-sm text-text-muted line-through">
                  {product.originalPrice.toLocaleString('vi-VN')}₫
                </span>
              )}
            </div>
            <motion.button
              whileHover={{ scale: 1.1 }}
              whileTap={{ scale: 0.95 }}
              onClick={(e) => {
                e.preventDefault();
                // TODO: add to cart
              }}
              className="flex h-9 w-9 cursor-pointer items-center justify-center rounded-full bg-brand text-white transition-shadow hover:shadow-lg"
            >
              <ShoppingCart className="h-4 w-4" />
            </motion.button>
          </div>
        </div>
      </Link>
    </motion.div>
  );
}
