import { useEffect, useState } from 'react';
import {
  Users,
  ShieldCheck,
  UserCircle,
  LogOut,
  Package,
  Plus,
  Pencil,
  Trash2,
  X,
  Tag,
  ShoppingBag,
} from 'lucide-react';
import { useNavigate } from 'react-router';
import apiClient from '@/api/client';
import { ENDPOINTS } from '@/api/endpoints';
import type { ApiResponse, PaginatedResponse } from '@/api/types';
import type {
  Category,
  CreateCategoryPayload,
  CreateProductPayload,
  Product,
} from '@/types/product';
import type { Order, OrderStatus } from '@/types/order';
import { ORDER_STATUS_COLOR, ORDER_STATUS_LABEL } from '@/types/order';
import { useAuthStore } from '@/store/useAuthStore';

interface UserItem {
  id: string;
  username: string;
  email: string;
  role: 'USER' | 'ADMIN';
  createdAt: string;
}

const emptyProductForm: CreateProductPayload = {
  name: '',
  brand: '',
  categoryId: '',
  price: 0,
  originalPrice: undefined,
  image: '',
  rating: 0,
  badge: '',
  specs: '',
};

const emptyCategoryForm: CreateCategoryPayload = {
  name: '',
  description: '',
  icon: '',
};

export function Component() {
  const [tab, setTab] = useState<
    'users' | 'products' | 'categories' | 'orders'
  >('users');

  // Users
  const [users, setUsers] = useState<UserItem[]>([]);
  const [totalUsers, setTotalUsers] = useState(0);
  const [loadingUsers, setLoadingUsers] = useState(true);

  // Products
  const [products, setProducts] = useState<Product[]>([]);
  const [loadingProducts, setLoadingProducts] = useState(true);
  const [showProductForm, setShowProductForm] = useState(false);
  const [editingProduct, setEditingProduct] = useState<Product | null>(null);
  const [productForm, setProductForm] =
    useState<CreateProductPayload>(emptyProductForm);
  const [savingProduct, setSavingProduct] = useState(false);

  // Categories
  const [categories, setCategories] = useState<Category[]>([]);
  const [loadingCategories, setLoadingCategories] = useState(true);
  const [showCategoryForm, setShowCategoryForm] = useState(false);
  const [editingCategory, setEditingCategory] = useState<Category | null>(null);
  const [categoryForm, setCategoryForm] =
    useState<CreateCategoryPayload>(emptyCategoryForm);
  const [savingCategory, setSavingCategory] = useState(false);

  // Orders
  const [orders, setOrders] = useState<Order[]>([]);
  const [loadingOrders, setLoadingOrders] = useState(true);

  const { user, logout } = useAuthStore();
  const navigate = useNavigate();

  useEffect(() => {
    apiClient
      .get<ApiResponse<PaginatedResponse<UserItem>>>('/users')
      .then((res) => {
        setUsers(res.data.data.content);
        setTotalUsers(res.data.data.totalElements);
      })
      .finally(() => setLoadingUsers(false));
  }, []);

  const fetchProducts = () => {
    setLoadingProducts(true);
    apiClient
      .get<ApiResponse<PaginatedResponse<Product>>>(ENDPOINTS.PRODUCTS.BASE, {
        params: { size: 100 },
      })
      .then((res) => setProducts(res.data.data.content))
      .finally(() => setLoadingProducts(false));
  };

  const fetchCategories = () => {
    setLoadingCategories(true);
    apiClient
      .get<ApiResponse<Category[]>>(ENDPOINTS.CATEGORIES.BASE)
      .then((res) => setCategories(res.data.data))
      .finally(() => setLoadingCategories(false));
  };

  const fetchOrders = () => {
    setLoadingOrders(true);
    apiClient
      .get<ApiResponse<PaginatedResponse<Order>>>(ENDPOINTS.ORDERS.BASE, {
        params: { size: 100 },
      })
      .then((res) => setOrders(res.data.data.content))
      .finally(() => setLoadingOrders(false));
  };

  useEffect(() => {
    fetchProducts();
    fetchCategories();
    fetchOrders();
  }, []);

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  // --- Product handlers ---
  const openCreateProduct = () => {
    setEditingProduct(null);
    setProductForm(emptyProductForm);
    setShowProductForm(true);
  };

  const openEditProduct = (p: Product) => {
    setEditingProduct(p);
    setProductForm({
      name: p.name,
      brand: p.brand,
      categoryId: p.categoryId ?? '',
      price: p.price,
      originalPrice: p.originalPrice,
      image: p.image,
      rating: p.rating,
      badge: p.badge ?? '',
      specs: p.specs ?? '',
    });
    setShowProductForm(true);
  };

  const handleSaveProduct = async () => {
    setSavingProduct(true);
    try {
      const payload = {
        ...productForm,
        categoryId: productForm.categoryId || undefined,
      };
      if (editingProduct) {
        await apiClient.put(
          ENDPOINTS.PRODUCTS.BY_ID(editingProduct.id),
          payload,
        );
      } else {
        await apiClient.post(ENDPOINTS.PRODUCTS.BASE, payload);
      }
      setShowProductForm(false);
      fetchProducts();
    } finally {
      setSavingProduct(false);
    }
  };

  const handleDeleteProduct = async (id: string) => {
    if (!window.confirm('Xác nhận xóa sản phẩm này?')) return;
    await apiClient.delete(ENDPOINTS.PRODUCTS.BY_ID(id));
    fetchProducts();
  };

  // --- Category handlers ---
  const openCreateCategory = () => {
    setEditingCategory(null);
    setCategoryForm(emptyCategoryForm);
    setShowCategoryForm(true);
  };

  const openEditCategory = (c: Category) => {
    setEditingCategory(c);
    setCategoryForm({
      name: c.name,
      description: c.description ?? '',
      icon: c.icon ?? '',
    });
    setShowCategoryForm(true);
  };

  const handleSaveCategory = async () => {
    setSavingCategory(true);
    try {
      if (editingCategory) {
        await apiClient.put(
          ENDPOINTS.CATEGORIES.BY_ID(editingCategory.id),
          categoryForm,
        );
      } else {
        await apiClient.post(ENDPOINTS.CATEGORIES.BASE, categoryForm);
      }
      setShowCategoryForm(false);
      fetchCategories();
    } finally {
      setSavingCategory(false);
    }
  };

  const handleDeleteCategory = async (id: string) => {
    if (!window.confirm('Xác nhận xóa danh mục này?')) return;
    await apiClient.delete(ENDPOINTS.CATEGORIES.BY_ID(id));
    fetchCategories();
  };

  const handleUpdateOrderStatus = async (id: string, status: OrderStatus) => {
    await apiClient.patch(ENDPOINTS.ORDERS.STATUS(id), null, {
      params: { status },
    });
    fetchOrders();
  };

  const userCount = users.filter((u) => u.role === 'USER').length;

  return (
    <div className="min-h-screen bg-surface-alt">
      {/* Header */}
      <header className="border-b border-border bg-surface px-8 py-4">
        <div className="mx-auto flex max-w-7xl items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-brand">
              <ShieldCheck className="h-5 w-5 text-white" />
            </div>
            <div>
              <h1 className="font-display text-lg font-bold text-text-primary">
                Admin Dashboard
              </h1>
              <p className="text-xs text-text-muted">NEBULA Management</p>
            </div>
          </div>
          <div className="flex items-center gap-4">
            <div className="flex items-center gap-2 text-sm text-text-secondary">
              <UserCircle className="h-5 w-5" />
              <span>{user?.username}</span>
              <span className="rounded-full bg-brand/10 px-2 py-0.5 text-xs font-medium text-brand">
                ADMIN
              </span>
            </div>
            <button
              type="button"
              onClick={handleLogout}
              className="flex items-center gap-2 rounded-lg border border-border px-3 py-1.5 text-sm text-text-secondary transition-colors hover:border-red-300 hover:text-red-500"
            >
              <LogOut className="h-4 w-4" />
              Đăng xuất
            </button>
          </div>
        </div>
      </header>

      <main className="mx-auto max-w-7xl px-8 py-8">
        {/* Stats */}
        <div className="mb-8 grid grid-cols-1 gap-4 sm:grid-cols-4">
          <div className="card p-6">
            <div className="flex items-center gap-4">
              <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-blue-50">
                <Users className="h-6 w-6 text-blue-600" />
              </div>
              <div>
                <p className="text-sm text-text-muted">Tổng người dùng</p>
                <p className="font-display text-2xl font-bold text-text-primary">
                  {totalUsers}
                </p>
              </div>
            </div>
          </div>
          <div className="card p-6">
            <div className="flex items-center gap-4">
              <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-green-50">
                <UserCircle className="h-6 w-6 text-green-600" />
              </div>
              <div>
                <p className="text-sm text-text-muted">User</p>
                <p className="font-display text-2xl font-bold text-text-primary">
                  {userCount}
                </p>
              </div>
            </div>
          </div>
          <div className="card p-6">
            <div className="flex items-center gap-4">
              <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-orange-50">
                <Package className="h-6 w-6 text-orange-600" />
              </div>
              <div>
                <p className="text-sm text-text-muted">Sản phẩm</p>
                <p className="font-display text-2xl font-bold text-text-primary">
                  {products.length}
                </p>
              </div>
            </div>
          </div>
          <div className="card p-6">
            <div className="flex items-center gap-4">
              <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-purple-50">
                <Tag className="h-6 w-6 text-purple-600" />
              </div>
              <div>
                <p className="text-sm text-text-muted">Danh mục</p>
                <p className="font-display text-2xl font-bold text-text-primary">
                  {categories.length}
                </p>
              </div>
            </div>
          </div>
          <div className="card p-6">
            <div className="flex items-center gap-4">
              <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-green-50">
                <ShoppingBag className="h-6 w-6 text-green-600" />
              </div>
              <div>
                <p className="text-sm text-text-muted">Đơn hàng</p>
                <p className="font-display text-2xl font-bold text-text-primary">
                  {orders.length}
                </p>
              </div>
            </div>
          </div>
        </div>

        {/* Tabs */}
        <div className="mb-6 flex gap-2">
          {(['users', 'products', 'categories', 'orders'] as const).map((t) => (
            <button
              key={t}
              type="button"
              onClick={() => setTab(t)}
              className={`rounded-lg px-4 py-2 text-sm font-medium transition-colors cursor-pointer ${
                tab === t
                  ? 'bg-brand text-white'
                  : 'bg-surface border border-border text-text-secondary hover:text-brand'
              }`}
            >
              {t === 'users'
                ? 'Người dùng'
                : t === 'products'
                  ? 'Sản phẩm'
                  : t === 'categories'
                    ? 'Danh mục'
                    : 'Đơn hàng'}
            </button>
          ))}
        </div>

        {/* Users Table */}
        {tab === 'users' && (
          <div className="card overflow-hidden">
            <div className="border-b border-border px-6 py-4">
              <h2 className="font-display text-base font-semibold text-text-primary">
                Danh sách người dùng
              </h2>
            </div>
            {loadingUsers ? (
              <div className="flex items-center justify-center py-16 text-text-muted">
                Đang tải...
              </div>
            ) : (
              <div className="overflow-x-auto">
                <table className="w-full text-sm">
                  <thead>
                    <tr className="border-b border-border bg-surface-alt text-left text-xs font-medium uppercase text-text-muted">
                      <th className="px-6 py-3">Username</th>
                      <th className="px-6 py-3">Email</th>
                      <th className="px-6 py-3">Role</th>
                      <th className="px-6 py-3">Ngày tạo</th>
                    </tr>
                  </thead>
                  <tbody>
                    {users.map((u) => (
                      <tr
                        key={u.id}
                        className="border-b border-border last:border-0 hover:bg-surface-alt"
                      >
                        <td className="px-6 py-4 font-medium text-text-primary">
                          {u.username}
                        </td>
                        <td className="px-6 py-4 text-text-secondary">
                          {u.email}
                        </td>
                        <td className="px-6 py-4">
                          <span
                            className={`rounded-full px-2.5 py-0.5 text-xs font-medium ${
                              u.role === 'ADMIN'
                                ? 'bg-purple-50 text-purple-700'
                                : 'bg-green-50 text-green-700'
                            }`}
                          >
                            {u.role}
                          </span>
                        </td>
                        <td className="px-6 py-4 text-text-muted">
                          {u.createdAt
                            ? new Date(u.createdAt).toLocaleDateString('vi-VN')
                            : '—'}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        )}

        {/* Products Table */}
        {tab === 'products' && (
          <div className="card overflow-hidden">
            <div className="flex items-center justify-between border-b border-border px-6 py-4">
              <h2 className="font-display text-base font-semibold text-text-primary">
                Quản lý sản phẩm
              </h2>
              <button
                type="button"
                onClick={openCreateProduct}
                className="flex cursor-pointer items-center gap-2 rounded-lg bg-brand px-4 py-2 text-sm font-medium text-white hover:bg-brand-accent"
              >
                <Plus className="h-4 w-4" /> Thêm sản phẩm
              </button>
            </div>
            {loadingProducts ? (
              <div className="flex items-center justify-center py-16 text-text-muted">
                Đang tải...
              </div>
            ) : (
              <div className="overflow-x-auto">
                <table className="w-full text-sm">
                  <thead>
                    <tr className="border-b border-border bg-surface-alt text-left text-xs font-medium uppercase text-text-muted">
                      <th className="px-4 py-3">Ảnh</th>
                      <th className="px-4 py-3">Tên</th>
                      <th className="px-4 py-3">Thương hiệu</th>
                      <th className="px-4 py-3">Danh mục</th>
                      <th className="px-4 py-3">Giá</th>
                      <th className="px-4 py-3">Rating</th>
                      <th className="px-4 py-3">Thao tác</th>
                    </tr>
                  </thead>
                  <tbody>
                    {products.map((p) => (
                      <tr
                        key={p.id}
                        className="border-b border-border last:border-0 hover:bg-surface-alt"
                      >
                        <td className="px-4 py-3">
                          <img
                            src={p.image}
                            alt={p.name}
                            className="h-12 w-12 rounded-lg object-contain bg-surface-alt"
                          />
                        </td>
                        <td className="px-4 py-3 font-medium text-text-primary max-w-[160px] truncate">
                          {p.name}
                        </td>
                        <td className="px-4 py-3 text-text-secondary">
                          {p.brand}
                        </td>
                        <td className="px-4 py-3">
                          {p.categoryName ? (
                            <span className="rounded-full bg-brand/10 px-2.5 py-0.5 text-xs font-medium text-brand">
                              {p.categoryName}
                            </span>
                          ) : (
                            <span className="text-text-muted">—</span>
                          )}
                        </td>
                        <td className="px-4 py-3 text-text-primary">
                          {p.price.toLocaleString('vi-VN')}₫
                        </td>
                        <td className="px-4 py-3 text-text-secondary">
                          {p.rating}
                        </td>
                        <td className="px-4 py-3">
                          <div className="flex gap-2">
                            <button
                              type="button"
                              onClick={() => openEditProduct(p)}
                              className="flex cursor-pointer items-center gap-1 rounded-lg border border-border px-2 py-1 text-xs text-text-secondary hover:border-brand hover:text-brand"
                            >
                              <Pencil className="h-3 w-3" /> Sửa
                            </button>
                            <button
                              type="button"
                              onClick={() => handleDeleteProduct(p.id)}
                              className="flex cursor-pointer items-center gap-1 rounded-lg border border-border px-2 py-1 text-xs text-text-secondary hover:border-red-300 hover:text-red-500"
                            >
                              <Trash2 className="h-3 w-3" /> Xóa
                            </button>
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        )}

        {/* Categories Table */}
        {tab === 'categories' && (
          <div className="card overflow-hidden">
            <div className="flex items-center justify-between border-b border-border px-6 py-4">
              <h2 className="font-display text-base font-semibold text-text-primary">
                Quản lý danh mục
              </h2>
              <button
                type="button"
                onClick={openCreateCategory}
                className="flex cursor-pointer items-center gap-2 rounded-lg bg-brand px-4 py-2 text-sm font-medium text-white hover:bg-brand-accent"
              >
                <Plus className="h-4 w-4" /> Thêm danh mục
              </button>
            </div>
            {loadingCategories ? (
              <div className="flex items-center justify-center py-16 text-text-muted">
                Đang tải...
              </div>
            ) : (
              <div className="overflow-x-auto">
                <table className="w-full text-sm">
                  <thead>
                    <tr className="border-b border-border bg-surface-alt text-left text-xs font-medium uppercase text-text-muted">
                      <th className="px-6 py-3">Tên danh mục</th>
                      <th className="px-6 py-3">Slug</th>
                      <th className="px-6 py-3">Mô tả</th>
                      <th className="px-6 py-3">Icon</th>
                      <th className="px-6 py-3">Thao tác</th>
                    </tr>
                  </thead>
                  <tbody>
                    {categories.map((c) => (
                      <tr
                        key={c.id}
                        className="border-b border-border last:border-0 hover:bg-surface-alt"
                      >
                        <td className="px-6 py-4 font-medium text-text-primary">
                          {c.name}
                        </td>
                        <td className="px-6 py-4 text-text-muted font-mono text-xs">
                          {c.slug}
                        </td>
                        <td className="px-6 py-4 text-text-secondary max-w-[200px] truncate">
                          {c.description || '—'}
                        </td>
                        <td className="px-6 py-4 text-text-muted">
                          {c.icon || '—'}
                        </td>
                        <td className="px-6 py-4">
                          <div className="flex gap-2">
                            <button
                              type="button"
                              onClick={() => openEditCategory(c)}
                              className="flex cursor-pointer items-center gap-1 rounded-lg border border-border px-2 py-1 text-xs text-text-secondary hover:border-brand hover:text-brand"
                            >
                              <Pencil className="h-3 w-3" /> Sửa
                            </button>
                            <button
                              type="button"
                              onClick={() => handleDeleteCategory(c.id)}
                              className="flex cursor-pointer items-center gap-1 rounded-lg border border-border px-2 py-1 text-xs text-text-secondary hover:border-red-300 hover:text-red-500"
                            >
                              <Trash2 className="h-3 w-3" /> Xóa
                            </button>
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        )}
        {/* Orders Table */}
        {tab === 'orders' && (
          <div className="card overflow-hidden">
            <div className="border-b border-border px-6 py-4">
              <h2 className="font-display text-base font-semibold text-text-primary">
                Quản lý đơn hàng
              </h2>
            </div>
            {loadingOrders ? (
              <div className="flex items-center justify-center py-16 text-text-muted">
                Đang tải...
              </div>
            ) : orders.length === 0 ? (
              <div className="flex items-center justify-center py-16 text-text-muted">
                Chưa có đơn hàng nào
              </div>
            ) : (
              <div className="overflow-x-auto">
                <table className="w-full text-sm">
                  <thead>
                    <tr className="border-b border-border bg-surface-alt text-left text-xs font-medium uppercase text-text-muted">
                      <th className="px-4 py-3">Mã đơn</th>
                      <th className="px-4 py-3">Khách hàng</th>
                      <th className="px-4 py-3">Sản phẩm</th>
                      <th className="px-4 py-3">Tổng tiền</th>
                      <th className="px-4 py-3">Thanh toán</th>
                      <th className="px-4 py-3">Trạng thái</th>
                      <th className="px-4 py-3">Cập nhật</th>
                    </tr>
                  </thead>
                  <tbody>
                    {orders.map((o) => (
                      <tr
                        key={o.id}
                        className="border-b border-border last:border-0 hover:bg-surface-alt"
                      >
                        <td className="px-4 py-3 font-mono text-xs text-text-muted">
                          {o.id.slice(-8).toUpperCase()}
                        </td>
                        <td className="px-4 py-3">
                          <p className="font-medium text-text-primary">
                            {o.customerName}
                          </p>
                          <p className="text-xs text-text-muted">{o.phone}</p>
                        </td>
                        <td className="px-4 py-3 text-text-secondary">
                          {o.items.length} sản phẩm
                        </td>
                        <td className="px-4 py-3 font-medium text-brand">
                          {o.total.toLocaleString('vi-VN')}₫
                        </td>
                        <td className="px-4 py-3 text-text-secondary text-xs">
                          {o.paymentMethod}
                        </td>
                        <td className="px-4 py-3">
                          <span
                            className={`rounded-full px-2.5 py-0.5 text-xs font-medium ${ORDER_STATUS_COLOR[o.status]}`}
                          >
                            {ORDER_STATUS_LABEL[o.status]}
                          </span>
                        </td>
                        <td className="px-4 py-3">
                          <select
                            value={o.status}
                            onChange={(e) =>
                              handleUpdateOrderStatus(
                                o.id,
                                e.target.value as OrderStatus,
                              )
                            }
                            className="cursor-pointer rounded-lg border border-border bg-surface px-2 py-1 text-xs text-text-secondary outline-none"
                          >
                            <option value="PENDING">Chờ xác nhận</option>
                            <option value="CONFIRMED">Đã xác nhận</option>
                            <option value="SHIPPING">Đang giao</option>
                            <option value="DELIVERED">Đã giao</option>
                            <option value="CANCELLED">Đã hủy</option>
                          </select>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        )}
      </main>

      {/* Product Form Modal */}
      {showProductForm && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4">
          <div className="w-full max-w-lg rounded-2xl bg-surface p-6 shadow-2xl">
            <div className="mb-4 flex items-center justify-between">
              <h3 className="font-display text-lg font-bold text-text-primary">
                {editingProduct ? 'Cập nhật sản phẩm' : 'Thêm sản phẩm mới'}
              </h3>
              <button
                type="button"
                onClick={() => setShowProductForm(false)}
                className="cursor-pointer text-text-muted hover:text-brand"
              >
                <X className="h-5 w-5" />
              </button>
            </div>
            <div className="space-y-3">
              {(
                [
                  ['name', 'Tên sản phẩm'],
                  ['brand', 'Thương hiệu'],
                  ['image', 'URL ảnh'],
                  ['specs', 'Thông số'],
                  ['badge', 'Badge (Hot/New/Sale...)'],
                ] as const
              ).map(([field, label]) => (
                <div key={field}>
                  <label className="mb-1 block text-xs font-medium text-text-secondary">
                    {label}
                  </label>
                  <input
                    type="text"
                    value={
                      ((productForm as unknown as Record<string, unknown>)[
                        field
                      ] as string) ?? ''
                    }
                    onChange={(e) =>
                      setProductForm({
                        ...productForm,
                        [field]: e.target.value,
                      })
                    }
                    className="w-full rounded-lg border border-border bg-surface-alt px-3 py-2 text-sm outline-none focus:border-brand"
                  />
                </div>
              ))}

              {/* Category select */}
              <div>
                <label className="mb-1 block text-xs font-medium text-text-secondary">
                  Danh mục
                </label>
                <select
                  value={productForm.categoryId ?? ''}
                  onChange={(e) =>
                    setProductForm({
                      ...productForm,
                      categoryId: e.target.value,
                    })
                  }
                  className="w-full rounded-lg border border-border bg-surface-alt px-3 py-2 text-sm outline-none focus:border-brand"
                >
                  <option value="">-- Không có danh mục --</option>
                  {categories.map((c) => (
                    <option key={c.id} value={c.id}>
                      {c.name}
                    </option>
                  ))}
                </select>
              </div>

              <div className="grid grid-cols-3 gap-3">
                <div>
                  <label className="mb-1 block text-xs font-medium text-text-secondary">
                    Giá (₫)
                  </label>
                  <input
                    type="number"
                    value={productForm.price}
                    onChange={(e) =>
                      setProductForm({
                        ...productForm,
                        price: Number(e.target.value),
                      })
                    }
                    className="w-full rounded-lg border border-border bg-surface-alt px-3 py-2 text-sm outline-none focus:border-brand"
                  />
                </div>
                <div>
                  <label className="mb-1 block text-xs font-medium text-text-secondary">
                    Giá gốc (₫)
                  </label>
                  <input
                    type="number"
                    value={productForm.originalPrice ?? ''}
                    onChange={(e) =>
                      setProductForm({
                        ...productForm,
                        originalPrice: e.target.value
                          ? Number(e.target.value)
                          : undefined,
                      })
                    }
                    className="w-full rounded-lg border border-border bg-surface-alt px-3 py-2 text-sm outline-none focus:border-brand"
                  />
                </div>
                <div>
                  <label className="mb-1 block text-xs font-medium text-text-secondary">
                    Rating
                  </label>
                  <input
                    type="number"
                    step="0.1"
                    min="0"
                    max="5"
                    value={productForm.rating ?? 0}
                    onChange={(e) =>
                      setProductForm({
                        ...productForm,
                        rating: Number(e.target.value),
                      })
                    }
                    className="w-full rounded-lg border border-border bg-surface-alt px-3 py-2 text-sm outline-none focus:border-brand"
                  />
                </div>
              </div>
            </div>
            <div className="mt-5 flex gap-3">
              <button
                type="button"
                onClick={() => setShowProductForm(false)}
                className="flex-1 cursor-pointer rounded-lg border border-border py-2 text-sm text-text-secondary hover:text-brand"
              >
                Hủy
              </button>
              <button
                type="button"
                onClick={handleSaveProduct}
                disabled={savingProduct}
                className="flex-1 cursor-pointer rounded-lg bg-brand py-2 text-sm font-medium text-white hover:bg-brand-accent disabled:opacity-60"
              >
                {savingProduct
                  ? 'Đang lưu...'
                  : editingProduct
                    ? 'Cập nhật'
                    : 'Tạo mới'}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Category Form Modal */}
      {showCategoryForm && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4">
          <div className="w-full max-w-md rounded-2xl bg-surface p-6 shadow-2xl">
            <div className="mb-4 flex items-center justify-between">
              <h3 className="font-display text-lg font-bold text-text-primary">
                {editingCategory ? 'Cập nhật danh mục' : 'Thêm danh mục mới'}
              </h3>
              <button
                type="button"
                onClick={() => setShowCategoryForm(false)}
                className="cursor-pointer text-text-muted hover:text-brand"
              >
                <X className="h-5 w-5" />
              </button>
            </div>
            <div className="space-y-3">
              <div>
                <label className="mb-1 block text-xs font-medium text-text-secondary">
                  Tên danh mục *
                </label>
                <input
                  type="text"
                  value={categoryForm.name}
                  onChange={(e) =>
                    setCategoryForm({ ...categoryForm, name: e.target.value })
                  }
                  className="w-full rounded-lg border border-border bg-surface-alt px-3 py-2 text-sm outline-none focus:border-brand"
                  placeholder="VD: Flagship, Tầm trung..."
                />
              </div>
              <div>
                <label className="mb-1 block text-xs font-medium text-text-secondary">
                  Mô tả
                </label>
                <input
                  type="text"
                  value={categoryForm.description ?? ''}
                  onChange={(e) =>
                    setCategoryForm({
                      ...categoryForm,
                      description: e.target.value,
                    })
                  }
                  className="w-full rounded-lg border border-border bg-surface-alt px-3 py-2 text-sm outline-none focus:border-brand"
                  placeholder="Mô tả ngắn về danh mục..."
                />
              </div>
              <div>
                <label className="mb-1 block text-xs font-medium text-text-secondary">
                  Icon (tên icon Lucide)
                </label>
                <input
                  type="text"
                  value={categoryForm.icon ?? ''}
                  onChange={(e) =>
                    setCategoryForm({ ...categoryForm, icon: e.target.value })
                  }
                  className="w-full rounded-lg border border-border bg-surface-alt px-3 py-2 text-sm outline-none focus:border-brand"
                  placeholder="VD: crown, smartphone, camera..."
                />
              </div>
            </div>
            <div className="mt-5 flex gap-3">
              <button
                type="button"
                onClick={() => setShowCategoryForm(false)}
                className="flex-1 cursor-pointer rounded-lg border border-border py-2 text-sm text-text-secondary hover:text-brand"
              >
                Hủy
              </button>
              <button
                type="button"
                onClick={handleSaveCategory}
                disabled={savingCategory || !categoryForm.name.trim()}
                className="flex-1 cursor-pointer rounded-lg bg-brand py-2 text-sm font-medium text-white hover:bg-brand-accent disabled:opacity-60"
              >
                {savingCategory
                  ? 'Đang lưu...'
                  : editingCategory
                    ? 'Cập nhật'
                    : 'Tạo mới'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
