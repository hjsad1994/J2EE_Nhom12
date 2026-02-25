import {
	ArrowLeft,
	Check,
	Heart,
	RotateCcw,
	Shield,
	ShoppingCart,
	Star,
	Truck,
} from "lucide-react";
import { motion } from "motion/react";
import { Link, useParams } from "react-router";
import ProductCard from "@/components/ui/ProductCard";
import { allProducts } from "@/data/products";
import { useWishlistStore } from "@/store/useWishlistStore";

export function Component() {
	const { id } = useParams<{ id: string }>();
	const product = allProducts.find((p) => p.id === id);
	const toggleWishlist = useWishlistStore((s) => s.toggle);
	const isWishlisted = useWishlistStore((s) => s.has(product?.id ?? ""));

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

	const related = allProducts
		.filter((p) => p.brand === product.brand && p.id !== product.id)
		.slice(0, 4);

	const discount = product.originalPrice
		? Math.round((1 - product.price / product.originalPrice) * 100)
		: null;

	return (
		<div className="min-h-screen bg-surface pt-24 pb-16">
			<div className="mx-auto max-w-7xl px-6">
				{/* Breadcrumb */}
				<motion.nav
					initial={{ opacity: 0 }}
					animate={{ opacity: 1 }}
					className="mb-8 flex items-center gap-2 text-sm text-text-muted"
				>
					<Link
						to="/"
						className="text-text-muted transition-colors hover:text-brand no-underline"
					>
						Trang chủ
					</Link>
					<span>/</span>
					<Link
						to="/products"
						className="text-text-muted transition-colors hover:text-brand no-underline"
					>
						Sản phẩm
					</Link>
					<span>/</span>
					<span className="text-text-secondary">{product.name}</span>
				</motion.nav>

				{/* Product detail */}
				<div className="grid grid-cols-1 gap-12 lg:grid-cols-2">
					{/* Image */}
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
							transition={{ type: "spring", stiffness: 100 }}
							whileHover={{ scale: 1.05 }}
						/>

						{product.badge && (
							<span className="absolute top-6 left-6 rounded-full bg-brand px-4 py-1.5 text-sm font-semibold text-white">
								{product.badge}
							</span>
						)}
					</motion.div>

					{/* Info */}
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

						{/* Rating */}
						<div className="mt-3 flex items-center gap-2">
							<div className="flex items-center gap-0.5">
								{Array.from({ length: 5 }).map((_, i) => (
									<Star
										key={i}
										className={`h-4 w-4 ${
											i < Math.floor(product.rating)
												? "fill-amber-400 text-amber-400"
												: "fill-transparent text-text-muted"
										}`}
									/>
								))}
							</div>
							<span className="text-sm text-text-secondary">
								{product.rating.toFixed(1)} · 128 đánh giá
							</span>
						</div>

						{/* Specs */}
						{product.specs && (
							<p className="mt-4 text-sm text-text-secondary">
								{product.specs}
							</p>
						)}

						{/* Price */}
						<div className="mt-6 flex items-end gap-3">
							<span className="font-display text-4xl font-bold text-brand">
								{product.price.toLocaleString("vi-VN")}₫
							</span>
							{product.originalPrice && (
								<>
									<span className="text-lg text-text-muted line-through">
										{product.originalPrice.toLocaleString("vi-VN")}₫
									</span>
									<span className="rounded-full bg-red-500 px-2.5 py-0.5 text-xs font-bold text-white">
										-{discount}%
									</span>
								</>
							)}
						</div>

						{/* Color options (mock) */}
						<div className="mt-6">
							<p className="mb-2 text-sm font-medium text-text-secondary">
								Màu sắc
							</p>
							<div className="flex gap-2">
								{[
									"bg-zinc-800",
									"bg-zinc-400",
									"bg-amber-700",
									"bg-blue-900",
								].map((color, i) => (
									<button
										key={color}
										type="button"
										className={`h-8 w-8 cursor-pointer rounded-full ${color} ring-2 ring-offset-2 ring-offset-surface transition-all ${
											i === 0
												? "ring-brand"
												: "ring-transparent hover:ring-border-strong"
										}`}
									/>
								))}
							</div>
						</div>

						{/* Storage (mock) */}
						<div className="mt-6">
							<p className="mb-2 text-sm font-medium text-text-secondary">
								Dung lượng
							</p>
							<div className="flex gap-2">
								{["128GB", "256GB", "512GB", "1TB"].map((size, i) => (
									<button
										key={size}
										type="button"
										className={`cursor-pointer rounded-lg border px-4 py-2 text-sm font-medium transition-all ${
											i === 1
												? "border-brand-accent bg-brand-subtle text-brand-accent"
												: "border-border bg-surface text-text-secondary hover:border-border-strong"
										}`}
									>
										{size}
									</button>
								))}
							</div>
						</div>

						{/* CTA */}
						<div className="mt-8 flex gap-3">
							<motion.button
								whileHover={{ scale: 1.02 }}
								whileTap={{ scale: 0.98 }}
								className="btn-primary flex flex-1 items-center justify-center gap-2 py-4"
							>
								<ShoppingCart className="h-5 w-5" />
								Thêm vào giỏ hàng
							</motion.button>
							<motion.button
								whileHover={{ scale: 1.02 }}
								whileTap={{ scale: 0.98 }}
								className="btn-outline px-6 py-4"
							>
								Mua ngay
							</motion.button>
							<motion.button
								whileHover={{ scale: 1.05 }}
								whileTap={{ scale: 0.95 }}
								onClick={() => product && toggleWishlist(product)}
								className={`flex aspect-square h-14 w-14 shrink-0 cursor-pointer items-center justify-center rounded-xl border-2 transition-colors ${
									isWishlisted
										? "border-red-200 bg-red-50 text-red-500 hover:border-red-300 hover:bg-red-100"
										: "border-border bg-surface text-text-secondary hover:border-brand-accent hover:text-brand-accent"
								}`}
								aria-label={
									isWishlisted ? "Bỏ yêu thích" : "Thêm vào yêu thích"
								}
							>
								<Heart
									className={`h-6 w-6 ${isWishlisted ? "fill-current" : ""}`}
								/>
							</motion.button>
						</div>

						{/* Services */}
						<div className="mt-8 grid grid-cols-3 gap-3">
							{[
								{ icon: Shield, label: "Bảo hành 12 tháng" },
								{ icon: Truck, label: "Miễn phí giao hàng" },
								{ icon: RotateCcw, label: "Đổi trả 30 ngày" },
							].map((s) => (
								<div
									key={s.label}
									className="flex flex-col items-center gap-1.5 rounded-xl bg-surface-alt p-3 text-center"
								>
									<s.icon className="h-5 w-5 text-brand-accent" />
									<span className="text-[11px] text-text-secondary">
										{s.label}
									</span>
								</div>
							))}
						</div>

						{/* Highlights */}
						<div className="mt-8 card p-5">
							<h3 className="mb-3 font-display text-sm font-semibold text-brand">
								Điểm nổi bật
							</h3>
							<ul className="space-y-2">
								{[
									"Màn hình Super AMOLED 120Hz",
									"Chip xử lý thế hệ mới nhất",
									"Camera AI chuyên nghiệp",
									"Sạc nhanh 100W",
									"Kháng nước IP68",
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

				{/* Related products */}
				{related.length > 0 && (
					<section className="mt-24">
						<h2 className="mb-8 font-display text-2xl font-bold text-brand">
							Sản phẩm cùng thương hiệu
						</h2>
						<div className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-4">
							{related.map((p, i) => (
								<ProductCard key={p.id} product={p} index={i} />
							))}
						</div>
					</section>
				)}
			</div>
		</div>
	);
}
