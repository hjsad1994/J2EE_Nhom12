import { Search, SlidersHorizontal, X } from 'lucide-react';
import { AnimatePresence, motion } from 'motion/react';
import { useMemo, useState } from 'react';
import ProductCard from '@/components/ui/ProductCard';
import { allProducts } from '@/data/products';

const brands = ['Tất cả', ...new Set(allProducts.map((p) => p.brand))];

export function Component() {
  const [search, setSearch] = useState('');
  const [selectedBrand, setSelectedBrand] = useState('Tất cả');
  const [sortBy, setSortBy] = useState<
    'default' | 'price-asc' | 'price-desc' | 'rating'
  >('default');
  const [showFilters, setShowFilters] = useState(false);

  const filtered = useMemo(() => {
    let result = allProducts;

    // Search
    if (search) {
      const q = search.toLowerCase();
      result = result.filter(
        (p) =>
          p.name.toLowerCase().includes(q) ||
          p.brand.toLowerCase().includes(q) ||
          p.specs?.toLowerCase().includes(q),
      );
    }

    // Brand filter
    if (selectedBrand !== 'Tất cả') {
      result = result.filter((p) => p.brand === selectedBrand);
    }

    // Sort
    switch (sortBy) {
      case 'price-asc':
        result = [...result].sort((a, b) => a.price - b.price);
        break;
      case 'price-desc':
        result = [...result].sort((a, b) => b.price - a.price);
        break;
      case 'rating':
        result = [...result].sort((a, b) => b.rating - a.rating);
        break;
    }

    return result;
  }, [search, selectedBrand, sortBy]);

  return (
    <div className="min-h-screen bg-surface pt-24 pb-16">
      <div className="mx-auto max-w-7xl px-6">
        {/* Header */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5 }}
          className="mb-8"
        >
          <h1 className="font-display text-4xl font-bold tracking-tight text-brand md:text-5xl">
            Tất cả sản phẩm
          </h1>
          <p className="mt-2 text-text-secondary">
            Khám phá {allProducts.length}+ điện thoại từ các thương hiệu hàng
            đầu
          </p>
        </motion.div>

        {/* Search & Filters */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5, delay: 0.1 }}
          className="mb-8 space-y-4"
        >
          {/* Search bar */}
          <div className="flex gap-3">
            <div className="relative flex-1">
              <Search className="absolute top-1/2 left-4 h-4 w-4 -translate-y-1/2 text-text-muted" />
              <input
                type="text"
                placeholder="Tìm kiếm điện thoại..."
                value={search}
                onChange={(e) => setSearch(e.target.value)}
                className="w-full rounded-xl border border-border bg-surface py-3 pr-4 pl-11 text-sm text-text-primary placeholder-text-muted outline-none transition-colors focus:border-brand-accent focus:ring-1 focus:ring-brand-accent/20"
              />
              {search && (
                <button
                  type="button"
                  onClick={() => setSearch('')}
                  className="absolute top-1/2 right-3 -translate-y-1/2 cursor-pointer text-text-muted hover:text-brand"
                >
                  <X className="h-4 w-4" />
                </button>
              )}
            </div>
            <button
              type="button"
              onClick={() => setShowFilters(!showFilters)}
              className={`flex items-center gap-2 rounded-xl border px-4 py-3 text-sm font-medium transition-colors cursor-pointer ${
                showFilters
                  ? 'border-brand-accent bg-brand-subtle text-brand-accent'
                  : 'border-border bg-surface text-text-secondary hover:border-border-strong hover:text-brand'
              }`}
            >
              <SlidersHorizontal className="h-4 w-4" />
              Bộ lọc
            </button>
          </div>

          {/* Filter panel */}
          <AnimatePresence>
            {showFilters && (
              <motion.div
                initial={{ opacity: 0, height: 0 }}
                animate={{ opacity: 1, height: 'auto' }}
                exit={{ opacity: 0, height: 0 }}
                className="overflow-hidden card p-4"
              >
                <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
                  {/* Brands */}
                  <div className="flex flex-wrap gap-2">
                    {brands.map((brand) => (
                      <button
                        key={brand}
                        type="button"
                        onClick={() => setSelectedBrand(brand)}
                        className={`cursor-pointer rounded-full px-3.5 py-1.5 text-xs font-medium transition-all ${
                          selectedBrand === brand
                            ? 'bg-brand text-white'
                            : 'bg-surface-alt text-text-secondary hover:bg-border hover:text-brand'
                        }`}
                      >
                        {brand}
                      </button>
                    ))}
                  </div>

                  {/* Sort */}
                  <select
                    value={sortBy}
                    onChange={(e) => setSortBy(e.target.value as typeof sortBy)}
                    className="cursor-pointer rounded-lg border border-border bg-surface px-3 py-2 text-xs text-text-secondary outline-none"
                  >
                    <option value="default">Mặc định</option>
                    <option value="price-asc">Giá: Thấp → Cao</option>
                    <option value="price-desc">Giá: Cao → Thấp</option>
                    <option value="rating">Đánh giá cao nhất</option>
                  </select>
                </div>
              </motion.div>
            )}
          </AnimatePresence>
        </motion.div>

        {/* Results count */}
        <p className="mb-6 text-sm text-text-muted">
          Hiển thị {filtered.length} sản phẩm
          {selectedBrand !== 'Tất cả' && (
            <span>
              {' '}
              · Thương hiệu:{' '}
              <span className="font-medium text-brand-accent">
                {selectedBrand}
              </span>
            </span>
          )}
        </p>

        {/* Product grid */}
        {filtered.length > 0 ? (
          <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
            {filtered.map((product, index) => (
              <ProductCard key={product.id} product={product} index={index} />
            ))}
          </div>
        ) : (
          <div className="flex flex-col items-center justify-center py-24 text-center">
            <Search className="mb-4 h-12 w-12 text-text-muted" />
            <h3 className="font-display text-lg font-semibold text-brand">
              Không tìm thấy sản phẩm
            </h3>
            <p className="mt-1 text-sm text-text-secondary">
              Thử tìm kiếm với từ khóa khác hoặc bỏ bộ lọc
            </p>
          </div>
        )}
      </div>
    </div>
  );
}
