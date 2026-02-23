import { ArrowRight, Lock, Mail, Smartphone, User } from "lucide-react";
import { AnimatePresence, motion } from "motion/react";
import { type FormEvent, useState } from "react";
import { Link } from "react-router";

export function Component() {
	const [isLogin, setIsLogin] = useState(true);

	const handleSubmit = (e: FormEvent) => {
		e.preventDefault();
		// TODO: Implement auth logic
	};

	return (
		<div className="flex min-h-[calc(100vh-80px)] items-center justify-center px-4 py-12 pt-24">
			<motion.div
				initial={{ opacity: 0, y: 20 }}
				animate={{ opacity: 1, y: 0 }}
				transition={{ duration: 0.4 }}
				className="w-full max-w-md"
			>
				<div className="card overflow-hidden bg-surface p-8">
					{/* Header */}
					<div className="mb-8 text-center">
						<div className="mx-auto mb-4 flex h-12 w-12 items-center justify-center rounded-xl bg-brand/10">
							<Smartphone className="h-6 w-6 text-brand" />
						</div>
						<h1 className="font-display text-2xl font-bold text-text-primary">
							{isLogin ? "Chào mừng trở lại" : "Tạo tài khoản"}
						</h1>
						<p className="mt-2 text-sm text-text-muted">
							{isLogin
								? "Đăng nhập để tiếp tục mua sắm"
								: "Đăng ký để bắt đầu trải nghiệm"}
						</p>
					</div>

					{/* Tabs */}
					<div className="mb-8 flex rounded-lg bg-surface-alt p-1">
						<button
							type="button"
							onClick={() => setIsLogin(true)}
							className={`flex-1 cursor-pointer rounded-md py-2 text-sm font-medium transition-all ${
								isLogin
									? "bg-surface text-brand shadow-sm"
									: "text-text-muted hover:text-text-secondary"
							}`}
						>
							Đăng nhập
						</button>
						<button
							type="button"
							onClick={() => setIsLogin(false)}
							className={`flex-1 cursor-pointer rounded-md py-2 text-sm font-medium transition-all ${
								!isLogin
									? "bg-surface text-brand shadow-sm"
									: "text-text-muted hover:text-text-secondary"
							}`}
						>
							Đăng ký
						</button>
					</div>

					{/* Form */}
					<form onSubmit={handleSubmit} className="space-y-4">
						<AnimatePresence mode="popLayout">
							{!isLogin && (
								<motion.div
									key="name-field"
									initial={{ opacity: 0, height: 0 }}
									animate={{ opacity: 1, height: "auto" }}
									exit={{ opacity: 0, height: 0 }}
									className="space-y-4 overflow-hidden"
								>
									<div className="space-y-1">
										<label
											htmlFor="fullName"
											className="text-xs font-medium text-text-secondary"
										>
											Họ và tên
										</label>
										<div className="relative">
											<User className="pointer-events-none absolute top-2.5 left-3 h-5 w-5 text-text-muted" />
											<input
												id="fullName"
												type="text"
												className="w-full rounded-lg border border-border bg-surface px-10 py-2.5 text-sm outline-none transition-colors focus:border-brand focus:ring-1 focus:ring-brand"
												placeholder="Nguyễn Văn A"
											/>
										</div>
									</div>
								</motion.div>
							)}
						</AnimatePresence>

						<div className="space-y-1">
							<label
								htmlFor="email"
								className="text-xs font-medium text-text-secondary"
							>
								Email
							</label>
							<div className="relative">
								<Mail className="pointer-events-none absolute top-2.5 left-3 h-5 w-5 text-text-muted" />
								<input
									id="email"
									type="email"
									className="w-full rounded-lg border border-border bg-surface px-10 py-2.5 text-sm outline-none transition-colors focus:border-brand focus:ring-1 focus:ring-brand"
									placeholder="email@example.com"
								/>
							</div>
						</div>

						<div className="space-y-1">
							<label
								htmlFor="password"
								className="text-xs font-medium text-text-secondary"
							>
								Mật khẩu
							</label>
							<div className="relative">
								<Lock className="pointer-events-none absolute top-2.5 left-3 h-5 w-5 text-text-muted" />
								<input
									id="password"
									type="password"
									className="w-full rounded-lg border border-border bg-surface px-10 py-2.5 text-sm outline-none transition-colors focus:border-brand focus:ring-1 focus:ring-brand"
									placeholder="••••••••"
								/>
							</div>
						</div>

						{isLogin && (
							<div className="flex items-center justify-between">
								<label
									htmlFor="remember"
									className="flex items-center gap-2 text-xs text-text-secondary"
								>
									<input
										id="remember"
										type="checkbox"
										className="rounded border-border text-brand focus:ring-brand"
									/>
									Ghi nhớ đăng nhập
								</label>
								<Link
									to="/forgot-password"
									className="text-xs font-medium text-brand hover:underline no-underline"
								>
									Quên mật khẩu?
								</Link>
							</div>
						)}

						<button
							type="submit"
							className="btn-primary flex w-full cursor-pointer items-center justify-center gap-2"
						>
							{isLogin ? "Đăng nhập" : "Tạo tài khoản"}
							<ArrowRight className="h-4 w-4" />
						</button>
					</form>
				</div>
			</motion.div>
		</div>
	);
}
