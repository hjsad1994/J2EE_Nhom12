import {
  ArrowLeft,
  Camera,
  Check,
  Heart,
  Loader2,
  RotateCcw,
  Send,
  Shield,
  ShoppingCart,
  Star,
  SquarePen,
  Trash2,
  Truck,
  X,
} from 'lucide-react';
import { motion } from 'motion/react';
import { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router';
import apiClient from '@/api/client';
import { ENDPOINTS } from '@/api/endpoints';
import type { ApiResponse, PaginatedResponse } from '@/api/types';
import ProductCard from '@/components/ui/ProductCard';
import { useAuthStore } from '@/store/useAuthStore';
import { useCartStore } from '@/store/useCartStore';
import { useWishlistStore } from '@/store/useWishlistStore';
import type { AbsaResult } from '@/types/absa';
import type { Product, ProductVariant } from '@/types/product';
import type { CreateReviewPayload, Review } from '@/types/review';

export function Component() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [product, setProduct] = useState<Product | null>(null);
  const [related, setRelated] = useState<Product[]>([]);
  const [loading, setLoading] = useState(true);
  const toggleWishlist = useWishlistStore((s) => s.toggle);
  const isWishlisted = useWishlistStore((s) => s.has(product?.id ?? ''));
  const addToCart = useCartStore((s) => s.addItem);
  const { isLoggedIn, isAdmin, user } = useAuthStore();

  const [reviews, setReviews] = useState<Review[]>([]);
  const [reviewsLoading, setReviewsLoading] = useState(false);
  const [reviewRating, setReviewRating] = useState(5);
  const [reviewHover, setReviewHover] = useState(0);
  const [reviewComment, setReviewComment] = useState('');
  const [reviewSubmitting, setReviewSubmitting] = useState(false);
  const [reviewError, setReviewError] = useState('');
  const [reviewImages, setReviewImages] = useState<string[]>([]);
  const [uploadingReviewImage, setUploadingReviewImage] = useState(false);
  const [editingReviewId, setEditingReviewId] = useState<string | null>(null);
  const [selectedColor, setSelectedColor] = useState('');
  const [selectedStorage, setSelectedStorage] = useState('');

  const myReview = reviews.find((r) => r.userId === user?.id);

  useEffect(() => {
    if (!myReview || editingReviewId) return;
    setReviewRating(5);
    setReviewComment('');
    setReviewImages([]);
  }, [myReview, editingReviewId]);

  useEffect(() => {
    if (!id) return;
    setLoading(true);
    apiClient
      .get<ApiResponse<Product>>(ENDPOINTS.PRODUCTS.BY_ID(id))
      .then((res) => {
        const currentProduct = res.data.data;
        setProduct(currentProduct);
        return apiClient
          .get<ApiResponse<PaginatedResponse<Product>>>(
            ENDPOINTS.PRODUCTS.BASE,
            {
              params: { size: 100 },
            },
          )
          .then((all) => {
            setRelated(
              all.data.data.content
                .filter(
                  (item) =>
                    item.brand === currentProduct.brand &&
                    item.id !== currentProduct.id,
                )
                .slice(0, 4),
            );
          });
      })
      .catch(() => setProduct(null))
      .finally(() => setLoading(false));
  }, [id]);

  useEffect(() => {
    if (!id) return;
    setReviewsLoading(true);
    apiClient
      .get<ApiResponse<Review[]>>(ENDPOINTS.REVIEWS.BASE, {
        params: { productId: id },
      })
      .then((res) => setReviews(res.data.data))
      .catch(() => setReviews([]))
      .finally(() => setReviewsLoading(false));
  }, [id]);

  const handleSubmitReview = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!id) return;
    if (!isLoggedIn) {
      setReviewError('Vui lòng đăng nhập để gửi đánh giá');
      return;
    }

    setReviewError('');
    setReviewSubmitting(true);

    try {
      const payload: CreateReviewPayload = {
        productId: id,
        rating: reviewRating,
        comment: reviewComment.trim(),
        images: reviewImages.length > 0 ? reviewImages : undefined,
      };

      if (editingReviewId) {
        const res = await apiClient.put<ApiResponse<Review>>(
          ENDPOINTS.REVIEWS.BY_ID(editingReviewId),
          payload,
        );
        setReviews((prev) =>
          prev.map((review) =>
            review.id === editingReviewId ? res.data.data : review,
          ),
        );
        setEditingReviewId(null);
      } else {
        const res = await apiClient.post<ApiResponse<Review>>(
          ENDPOINTS.REVIEWS.BASE,
          payload,
        );
        setReviews((prev) => [res.data.data, ...prev]);
      }

      setReviewComment('');
      setReviewRating(5);
      setReviewImages([]);
    } catch (err: unknown) {
      const axiosErr = err as { response?: { data?: { message?: string } } };
      setReviewError(
        axiosErr.response?.data?.message ??
          'Không thể gửi đánh giá, thử lại sau',
      );
    } finally {
      setReviewSubmitting(false);
    }
  };

  const handleDeleteReview = async (reviewId: string) => {
    try {
      await apiClient.delete(ENDPOINTS.REVIEWS.BY_ID(reviewId));
      setReviews((prev) => prev.filter((review) => review.id !== reviewId));
    } catch {
      // silent fail
    }
  };

  const handleStartEditReview = (review: Review) => {
    setEditingReviewId(review.id);
    setReviewRating(review.rating);
    setReviewComment(review.comment);
    setReviewImages(review.images ?? []);
    setReviewError('');
  };

  const handleCancelEditReview = () => {
    setEditingReviewId(null);
    setReviewRating(5);
    setReviewComment('');
    setReviewImages([]);
    setReviewError('');
  };

  if (loading) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-surface pt-20 text-text-muted">
        Đang tải...
      </div>
    );
  }

  if (!product) {
    return (
      <div className="flex min-h-screen flex-col items-center justify-center bg-surface pt-20">
        <h1 className="font-display text-2xl font-bold text-brand">
          Sản phẩm không tồn tại
        </h1>
        <p className="mt-2 text-text-secondary">
          Sản phẩm bạn tìm kiếm không có trong hệ thống.
        </p>
        <Link
          to="/products"
          className="btn-primary mt-6 inline-flex items-center gap-2 no-underline"
        >
          <ArrowLeft className="h-4 w-4" /> Quay lại cửa hàng
        </Link>
      </div>
    );
  }

  const hasVariants = (product.variants?.length ?? 0) > 0;
  const uniqueColors = hasVariants
    ? [...new Set(product.variants!.map((v) => v.color))]
    : [];
  const uniqueStorages = hasVariants
    ? [...new Set(product.variants!.map((v) => v.storage))]
    : [];

  const selectedVariant: ProductVariant | null =
    hasVariants && selectedColor && selectedStorage
      ? (product.variants!.find(
          (v) => v.color === selectedColor && v.storage === selectedStorage,
        ) ?? null)
      : null;

  const effectivePrice = selectedVariant
    ? selectedVariant.price
    : product.price;
  // -1 means variants exist but none selected yet
  const effectiveStock = hasVariants
    ? selectedVariant
      ? selectedVariant.stock
      : -1
    : product.stock;

  const canAddToCart =
    effectiveStock > 0 &&
    (!hasVariants ||
      (selectedColor !== '' &&
        selectedStorage !== '' &&
        selectedVariant !== null));

  const handleColorSelect = (color: string) => {
    setSelectedColor(color);
    const storagesForColor =
      product.variants
        ?.filter((v) => v.color === color)
        .map((v) => v.storage) ?? [];
    if (selectedStorage && !storagesForColor.includes(selectedStorage)) {
      setSelectedStorage('');
    }
  };

  const discount = product.originalPrice
    ? Math.round((1 - effectivePrice / product.originalPrice) * 100)
    : null;

  const sentimentStyles: Record<AbsaResult['sentiment'], string> = {
    positive: 'border-emerald-200 bg-emerald-50 text-emerald-700',
    negative: 'border-amber-200 bg-amber-50 text-amber-700',
    neutral: 'border-slate-200 bg-slate-50 text-slate-700',
  };

  const sentimentLabels: Record<AbsaResult['sentiment'], string> = {
    positive: 'tốt',
    negative: 'chưa tốt',
    neutral: 'bình thường',
  };

  const aspectLabels: Record<string, string> = {
    Battery: 'Pin',
    Camera: 'Camera',
    Performance: 'Hiệu năng',
    Display: 'Màn hình',
    Design: 'Thiết kế',
    Packaging: 'Đóng gói',
    Price: 'Giá',
    Shop_Service: 'Dịch vụ cửa hàng',
    Shipping: 'Giao hàng',
    General: 'Tổng thể',
  };

  return (
    <div className="min-h-screen bg-surface pb-16 pt-24">
      <div className="mx-auto max-w-7xl px-6">
        <motion.nav
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          className="mb-8 flex items-center gap-2 text-sm text-text-muted"
        >
          <Link
            to="/"
            className="text-text-muted no-underline transition-colors hover:text-brand"
          >
            Trang chủ
          </Link>
          <span>/</span>
          <Link
            to="/products"
            className="text-text-muted no-underline transition-colors hover:text-brand"
          >
            Sản phẩm
          </Link>
          <span>/</span>
          <span className="text-text-secondary">{product.name}</span>
        </motion.nav>

        <div className="grid grid-cols-1 gap-12 lg:grid-cols-2">
          <motion.div
            initial={{ opacity: 0, x: -100 }}
            animate={{ opacity: 1, x: 0 }}
            transition={{ duration: 0.6 }}
            className="relative flex items-center justify-center rounded-3xl bg-surface-alt p-12"
          >
            <motion.img
              src={product.image}
              alt={product.name}
              className="relative z-10 max-h-[400px] w-auto object-contain"
              initial={{ scale: 0.9 }}
              animate={{ scale: 1 }}
              transition={{ type: 'spring', stiffness: 100 }}
              whileHover={{ scale: 1.05 }}
            />

            {product.badge && (
              <span className="absolute left-6 top-6 rounded-full bg-brand px-4 py-1.5 text-sm font-semibold text-white">
                {product.badge}
              </span>
            )}
          </motion.div>

          <motion.div
            initial={{ opacity: 0, x: 100 }}
            animate={{ opacity: 1, x: 0 }}
            transition={{ duration: 0.6, delay: 0.1 }}
          >
            <p className="text-sm font-medium uppercase tracking-widest text-brand-accent">
              {product.brand}
            </p>
            <h1 className="mt-2 font-display text-3xl font-bold tracking-tight text-brand md:text-4xl">
              {product.name}
            </h1>

            <div className="mt-3 flex items-center gap-2">
              <div className="flex items-center gap-0.5">
                {Array.from({ length: 5 }).map((_, i) => (
                  <Star
                    key={i}
                    className={`h-4 w-4 ${
                      i < Math.floor(product.rating)
                        ? 'fill-amber-400 text-amber-400'
                        : 'fill-transparent text-text-muted'
                    }`}
                  />
                ))}
              </div>
              <span className="text-sm text-text-secondary">
                {product.rating.toFixed(1)} · {reviews.length} đánh giá
              </span>
            </div>

            {product.specs && (
              <p className="mt-4 text-sm text-text-secondary">
                {product.specs}
              </p>
            )}

            <div className="mt-6 flex items-end gap-3">
              <span className="font-display text-4xl font-bold text-brand">
                {effectivePrice.toLocaleString('vi-VN')}đ
              </span>
              {product.originalPrice && (
                <>
                  <span className="text-lg text-text-muted line-through">
                    {product.originalPrice.toLocaleString('vi-VN')}đ
                  </span>
                  <span className="rounded-full bg-red-500 px-2.5 py-0.5 text-xs font-bold text-white">
                    -{discount}%
                  </span>
                </>
              )}
            </div>

            {hasVariants && (
              <>
                <div className="mt-6">
                  <p className="mb-2 text-sm font-medium text-text-secondary">
                    Màu sắc
                  </p>
                  <div className="flex flex-wrap gap-2">
                    {uniqueColors.map((color) => (
                      <button
                        key={color}
                        type="button"
                        onClick={() => handleColorSelect(color)}
                        className={`cursor-pointer rounded-lg border px-4 py-2 text-sm font-medium transition-all ${
                          selectedColor === color
                            ? 'border-brand-accent bg-brand-subtle text-brand-accent'
                            : 'border-border bg-surface text-text-secondary hover:border-border-strong'
                        }`}
                      >
                        {color}
                      </button>
                    ))}
                  </div>
                </div>

                <div className="mt-6">
                  <p className="mb-2 text-sm font-medium text-text-secondary">
                    Dung lượng
                  </p>
                  <div className="flex flex-wrap gap-2">
                    {uniqueStorages.map((storage) => {
                      const isAvailable =
                        !selectedColor ||
                        product.variants!.some(
                          (v) =>
                            v.color === selectedColor && v.storage === storage,
                        );
                      return (
                        <button
                          key={storage}
                          type="button"
                          disabled={!isAvailable}
                          onClick={() => setSelectedStorage(storage)}
                          className={`cursor-pointer rounded-lg border px-4 py-2 text-sm font-medium transition-all disabled:cursor-not-allowed disabled:opacity-40 ${
                            selectedStorage === storage
                              ? 'border-brand-accent bg-brand-subtle text-brand-accent'
                              : 'border-border bg-surface text-text-secondary hover:border-border-strong'
                          }`}
                        >
                          {storage}
                        </button>
                      );
                    })}
                  </div>
                </div>

                {selectedColor && selectedStorage && !selectedVariant && (
                  <p className="mt-2 text-sm text-red-500">
                    Phiên bản này hiện không có sẵn.
                  </p>
                )}
                {(!selectedColor || !selectedStorage) && (
                  <p className="mt-3 text-sm text-amber-600">
                    Vui lòng chọn màu sắc và dung lượng để xem giá và tình trạng
                    hàng.
                  </p>
                )}
              </>
            )}

            <div className="mt-6">
              {effectiveStock === -1 ? null : effectiveStock > 0 ? (
                <span className="inline-flex items-center gap-1.5 rounded-full bg-green-50 px-3 py-1 text-sm font-medium text-green-700">
                  <Check className="h-3.5 w-3.5" />
                  Còn hàng ({effectiveStock} sản phẩm)
                </span>
              ) : (
                <span className="inline-flex items-center rounded-full bg-red-50 px-3 py-1 text-sm font-medium text-red-700">
                  Hết hàng
                </span>
              )}
            </div>

            {!isAdmin &&
              (effectiveStock > 0 ||
                (hasVariants && effectiveStock === -1)) && (
                <div className="mt-8 flex gap-3">
                  <motion.button
                    whileHover={{ scale: canAddToCart ? 1.02 : 1 }}
                    whileTap={{ scale: canAddToCart ? 0.98 : 1 }}
                    disabled={!canAddToCart}
                    onClick={() => {
                      if (!isLoggedIn) {
                        navigate('/login');
                        return;
                      }
                      if (canAddToCart)
                        addToCart({
                          ...product,
                          price: effectivePrice,
                          stock: effectiveStock,
                        });
                    }}
                    className="btn-primary flex flex-1 items-center justify-center gap-2 py-4 disabled:opacity-50 disabled:cursor-not-allowed"
                  >
                    <ShoppingCart className="h-5 w-5" />
                    Thêm vào giỏ hàng
                  </motion.button>
                  <motion.button
                    whileHover={{ scale: canAddToCart ? 1.02 : 1 }}
                    whileTap={{ scale: canAddToCart ? 0.98 : 1 }}
                    disabled={!canAddToCart}
                    onClick={() => {
                      if (!isLoggedIn) {
                        navigate('/login');
                        return;
                      }
                      if (!canAddToCart) return;
                      addToCart({
                        ...product,
                        price: effectivePrice,
                        stock: effectiveStock,
                      });
                      navigate('/checkout');
                    }}
                    className="btn-outline px-6 py-4 disabled:opacity-50 disabled:cursor-not-allowed"
                  >
                    Mua ngay
                  </motion.button>
                  <motion.button
                    whileHover={{ scale: 1.05 }}
                    whileTap={{ scale: 0.95 }}
                    onClick={() => {
                      if (!isLoggedIn) {
                        navigate('/login');
                        return;
                      }
                      toggleWishlist(product);
                    }}
                    className={`flex h-14 w-14 shrink-0 cursor-pointer items-center justify-center rounded-xl border-2 transition-colors ${
                      isWishlisted
                        ? 'border-red-200 bg-red-50 text-red-500 hover:border-red-300 hover:bg-red-100'
                        : 'border-border bg-surface text-text-secondary hover:border-brand-accent hover:text-brand-accent'
                    }`}
                    aria-label={
                      isWishlisted ? 'Bỏ yêu thích' : 'Thêm vào yêu thích'
                    }
                  >
                    <Heart
                      className={`h-6 w-6 ${isWishlisted ? 'fill-current' : ''}`}
                    />
                  </motion.button>
                </div>
              )}

            <div className="mt-8 grid grid-cols-3 gap-3">
              {[
                { icon: Shield, label: 'Bảo hành 12 tháng' },
                { icon: Truck, label: 'Miễn phí giao hàng' },
                { icon: RotateCcw, label: 'Đổi trả 30 ngày' },
              ].map((item) => (
                <div
                  key={item.label}
                  className="flex flex-col items-center gap-1.5 rounded-xl bg-surface-alt p-3 text-center"
                >
                  <item.icon className="h-5 w-5 text-brand-accent" />
                  <span className="text-[11px] text-text-secondary">
                    {item.label}
                  </span>
                </div>
              ))}
            </div>

            <div className="mt-8 card p-5">
              <h3 className="mb-3 font-display text-sm font-semibold text-brand">
                Điểm nổi bật
              </h3>
              <ul className="space-y-2">
                {[
                  'Màn hình Super AMOLED 120Hz',
                  'Chip xử lý thế hệ mới nhất',
                  'Camera AI chuyên nghiệp',
                  'Sạc nhanh 100W',
                  'Kháng nước IP68',
                ].map((item) => (
                  <li
                    key={item}
                    className="flex items-center gap-2 text-sm text-text-secondary"
                  >
                    <Check className="h-4 w-4 shrink-0 text-emerald-500" />
                    {item}
                  </li>
                ))}
              </ul>
            </div>
          </motion.div>
        </div>

        <section className="mt-20">
          <div className="mb-6 flex items-center justify-between">
            <h2 className="font-display text-2xl font-bold text-brand">
              Đánh giá sản phẩm
            </h2>
            <span className="text-sm text-text-secondary">
              {reviews.length} đánh giá
            </span>
          </div>

          {isLoggedIn && !isAdmin && (!myReview || editingReviewId) && (
            <motion.form
              initial={{ opacity: 0, y: 10 }}
              animate={{ opacity: 1, y: 0 }}
              onSubmit={handleSubmitReview}
              className="mb-8 rounded-2xl border border-border bg-surface p-6"
            >
              <p className="mb-4 font-medium text-text-primary">
                {editingReviewId
                  ? 'Chỉnh sửa đánh giá của bạn'
                  : 'Viết đánh giá của bạn'}
              </p>

              <div className="mb-4 flex items-center gap-1">
                {[1, 2, 3, 4, 5].map((star) => (
                  <button
                    key={star}
                    type="button"
                    onClick={() => setReviewRating(star)}
                    onMouseEnter={() => setReviewHover(star)}
                    onMouseLeave={() => setReviewHover(0)}
                    className="cursor-pointer p-0.5 transition-transform hover:scale-110"
                    aria-label={`${star} sao`}
                  >
                    <Star
                      className={`h-7 w-7 transition-colors ${
                        star <= (reviewHover || reviewRating)
                          ? 'fill-amber-400 text-amber-400'
                          : 'fill-transparent text-text-muted'
                      }`}
                    />
                  </button>
                ))}
                <span className="ml-2 text-sm text-text-secondary">
                  {reviewRating} / 5
                </span>
              </div>

              <textarea
                value={reviewComment}
                onChange={(e) => setReviewComment(e.target.value)}
                placeholder="Chia sẻ trải nghiệm của bạn về sản phẩm này..."
                rows={3}
                maxLength={1000}
                required
                className="w-full resize-none rounded-xl border border-border bg-surface-alt px-4 py-3 text-sm text-text-primary outline-none transition-colors placeholder:text-text-muted focus:border-brand focus:ring-1 focus:ring-brand"
              />

              <div className="mt-1 flex items-center justify-between">
                <span className="text-xs text-text-muted">
                  {reviewComment.length}/1000
                </span>
              </div>

              <div className="mt-3">
                <div className="flex flex-wrap gap-2">
                  {reviewImages.map((url, index) => (
                    <div key={index} className="relative">
                      <img
                        src={url}
                        alt={`Review ${index + 1}`}
                        className="h-20 w-20 rounded-lg border border-border object-cover"
                      />
                      <button
                        type="button"
                        onClick={() =>
                          setReviewImages((prev) =>
                            prev.filter(
                              (_, imageIndex) => imageIndex !== index,
                            ),
                          )
                        }
                        className="absolute -right-1.5 -top-1.5 cursor-pointer rounded-full bg-red-500 p-0.5 text-white hover:bg-red-600"
                      >
                        <X className="h-3 w-3" />
                      </button>
                    </div>
                  ))}

                  {reviewImages.length < 5 && (
                    <label
                      className={`flex h-20 w-20 cursor-pointer flex-col items-center justify-center gap-1 rounded-lg border-2 border-dashed transition-colors ${
                        uploadingReviewImage
                          ? 'border-brand/30 bg-brand/5'
                          : 'border-border hover:border-brand hover:bg-brand/5'
                      }`}
                    >
                      {uploadingReviewImage ? (
                        <Loader2 className="h-5 w-5 animate-spin text-brand" />
                      ) : (
                        <>
                          <Camera className="h-5 w-5 text-text-muted" />
                          <span className="text-[10px] text-text-muted">
                            Thêm ảnh
                          </span>
                        </>
                      )}
                      <input
                        type="file"
                        accept="image/jpeg,image/png,image/webp,image/gif"
                        className="hidden"
                        disabled={uploadingReviewImage}
                        onChange={async (e) => {
                          const file = e.target.files?.[0];
                          if (!file) return;
                          e.target.value = '';
                          setUploadingReviewImage(true);
                          try {
                            const formData = new FormData();
                            formData.append('file', file);
                            const res = await apiClient.post<
                              ApiResponse<string>
                            >(ENDPOINTS.REVIEWS.UPLOAD_IMAGE, formData, {
                              headers: {
                                'Content-Type': 'multipart/form-data',
                              },
                            });
                            setReviewImages((prev) => [...prev, res.data.data]);
                          } catch {
                            setReviewError('Upload ảnh thất bại');
                          } finally {
                            setUploadingReviewImage(false);
                          }
                        }}
                      />
                    </label>
                  )}
                </div>
                <p className="mt-1 text-xs text-text-muted">
                  {reviewImages.length}/5 ảnh (JPEG, PNG, WebP, GIF)
                </p>
              </div>

              {reviewError && (
                <p className="mt-2 rounded-lg bg-red-50 px-3 py-2 text-sm text-red-600">
                  {reviewError}
                </p>
              )}

              <div className="mt-4 flex justify-end">
                {editingReviewId && (
                  <motion.button
                    type="button"
                    onClick={handleCancelEditReview}
                    whileHover={{ scale: 1.02 }}
                    whileTap={{ scale: 0.98 }}
                    className="mr-3 cursor-pointer rounded-xl border border-border px-4 py-2 text-sm font-medium text-text-secondary transition-colors hover:border-border-strong hover:text-text-primary"
                  >
                    Hủy
                  </motion.button>
                )}
                <motion.button
                  type="submit"
                  disabled={reviewSubmitting || !reviewComment.trim()}
                  whileHover={{ scale: 1.02 }}
                  whileTap={{ scale: 0.98 }}
                  className="btn-primary flex cursor-pointer items-center gap-2 disabled:opacity-60"
                >
                  {reviewSubmitting ? (
                    <Loader2 className="h-4 w-4 animate-spin" />
                  ) : (
                    <Send className="h-4 w-4" />
                  )}
                  {editingReviewId ? 'Lưu thay đổi' : 'Gửi đánh giá'}
                </motion.button>
              </div>
            </motion.form>
          )}

          {!isLoggedIn && (
            <div className="mb-8 rounded-2xl border border-border bg-surface-alt px-6 py-5 text-center">
              <p className="text-sm text-text-secondary">
                <Link
                  to="/login"
                  className="font-medium text-brand hover:underline"
                >
                  Đăng nhập
                </Link>{' '}
                để viết đánh giá sản phẩm.
              </p>
            </div>
          )}

          {isLoggedIn && !isAdmin && myReview && !editingReviewId && (
            <div className="mb-8 rounded-2xl border border-border bg-surface-alt px-6 py-4">
              <div className="flex items-center justify-between gap-3">
                <p className="text-sm text-text-secondary">
                  Bạn đã gửi đánh giá cho sản phẩm này.
                </p>
                <button
                  type="button"
                  onClick={() => handleStartEditReview(myReview)}
                  className="inline-flex cursor-pointer items-center gap-2 rounded-xl border border-border bg-surface px-4 py-2 text-sm font-medium text-text-primary transition-colors hover:border-brand hover:text-brand"
                >
                  <SquarePen className="h-4 w-4" />
                  Chỉnh sửa đánh giá
                </button>
              </div>
            </div>
          )}

          {reviewsLoading ? (
            <div className="flex justify-center py-10">
              <Loader2 className="h-6 w-6 animate-spin text-text-muted" />
            </div>
          ) : reviews.length === 0 ? (
            <div className="flex flex-col items-center py-12 text-center">
              <Star className="h-10 w-10 text-text-muted" />
              <p className="mt-3 text-text-secondary">
                Chưa có đánh giá nào. Hãy là người đầu tiên!
              </p>
            </div>
          ) : (
            <div className="space-y-4">
              {reviews.map((review) => (
                <motion.div
                  key={review.id}
                  initial={{ opacity: 0, y: 10 }}
                  animate={{ opacity: 1, y: 0 }}
                  className="rounded-2xl border border-border bg-surface p-5"
                >
                  <div className="flex items-start justify-between gap-4">
                    <div className="flex items-center gap-3">
                      <div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-full bg-brand/10 font-semibold text-brand">
                        {review.username.charAt(0).toUpperCase()}
                      </div>
                      <p className="text-sm font-semibold text-text-primary">
                        {review.username}
                      </p>
                    </div>

                    <div className="flex items-center gap-2">
                      <div className="flex items-center gap-0.5">
                        {[1, 2, 3, 4, 5].map((star) => (
                          <Star
                            key={star}
                            className={`h-3.5 w-3.5 ${
                              star <= review.rating
                                ? 'fill-amber-400 text-amber-400'
                                : 'fill-transparent text-text-muted'
                            }`}
                          />
                        ))}
                      </div>
                      {review.userId === user?.id && (
                        <div className="flex items-center gap-1">
                          <button
                            type="button"
                            onClick={() => handleStartEditReview(review)}
                            className="cursor-pointer rounded-lg p-1.5 text-text-muted transition-colors hover:bg-blue-50 hover:text-blue-500"
                            aria-label="Sửa đánh giá"
                          >
                            <SquarePen className="h-3.5 w-3.5" />
                          </button>
                          <button
                            type="button"
                            onClick={() => handleDeleteReview(review.id)}
                            className="cursor-pointer rounded-lg p-1.5 text-text-muted transition-colors hover:bg-red-50 hover:text-red-500"
                            aria-label="Xóa đánh giá"
                          >
                            <Trash2 className="h-3.5 w-3.5" />
                          </button>
                        </div>
                      )}
                    </div>
                  </div>

                  <p className="mt-3 text-sm leading-relaxed text-text-secondary">
                    {review.comment}
                  </p>

                  {review.analysisResults &&
                    review.analysisResults.length > 0 && (
                      <div className="mt-3 flex flex-wrap gap-2">
                        {review.analysisResults.map((result) => (
                          <div
                            key={`${review.id}-${result.aspect}-${result.sentiment}`}
                            className={`inline-flex items-center gap-2 rounded-full border px-3 py-1.5 text-sm font-medium shadow-sm ${sentimentStyles[result.sentiment]}`}
                          >
                            <span className="text-text-primary">
                              {aspectLabels[result.aspect] ?? result.aspect}
                            </span>
                            <span className="h-1.5 w-1.5 rounded-full bg-current opacity-70" />
                            <span>{sentimentLabels[result.sentiment]}</span>
                          </div>
                        ))}
                      </div>
                    )}

                  {review.images && review.images.length > 0 && (
                    <div className="mt-3 flex flex-wrap gap-2">
                      {review.images.map((img, index) => (
                        <a
                          key={index}
                          href={img}
                          target="_blank"
                          rel="noopener noreferrer"
                        >
                          <img
                            src={img}
                            alt={`Review ảnh ${index + 1}`}
                            className="h-20 w-20 rounded-lg border border-border object-cover transition-transform hover:scale-105"
                          />
                        </a>
                      ))}
                    </div>
                  )}
                </motion.div>
              ))}
            </div>
          )}
        </section>

        {related.length > 0 && (
          <section className="mt-24">
            <h2 className="mb-8 font-display text-2xl font-bold text-brand">
              Sản phẩm cùng thương hiệu
            </h2>
            <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-4">
              {related.map((item, index) => (
                <ProductCard key={item.id} product={item} index={index} />
              ))}
            </div>
          </section>
        )}
      </div>
    </div>
  );
}
