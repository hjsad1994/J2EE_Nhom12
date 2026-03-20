import { Plus, Pencil, TicketPercent, Trash2, X } from 'lucide-react';
import { useEffect, useState } from 'react';

import apiClient from '@/api/client';
import { ENDPOINTS } from '@/api/endpoints';
import type { ApiResponse } from '@/api/types';
import { useToastStore } from '@/store/useToastStore';
import type {
  Voucher,
  VoucherDiscountType,
  VoucherFormPayload,
  VoucherType,
} from '@/types/voucher';

interface ApiError {
  response?: {
    data?: {
      message?: string;
    };
  };
}

const emptyVoucherForm: VoucherFormPayload = {
  code: '',
  description: '',
  type: 'PRODUCT',
  discountType: 'PERCENTAGE',
  discountValue: 0,
  maxDiscountAmount: null,
  minOrderValue: 0,
  usageLimit: null,
  startAt: null,
  endAt: null,
  active: true,
};

const inputClass =
  'w-full rounded-xl border border-gray-200 bg-gray-50 px-3 py-2.5 text-sm outline-none focus:border-purple-400 focus:bg-white focus:ring-2 focus:ring-purple-100';

const typeLabel: Record<VoucherType, string> = {
  PRODUCT: 'Giảm sản phẩm',
  SHIPPING: 'Freeship',
};

const discountTypeLabel: Record<VoucherDiscountType, string> = {
  PERCENTAGE: 'Phần trăm',
  FIXED_AMOUNT: 'Số tiền',
};

export default function VoucherManagement() {
  const [vouchers, setVouchers] = useState<Voucher[]>([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [showForm, setShowForm] = useState(false);
  const [editingVoucher, setEditingVoucher] = useState<Voucher | null>(null);
  const [form, setForm] = useState<VoucherFormPayload>(emptyVoucherForm);
  const addToast = useToastStore((s) => s.addToast);

  const fetchVouchers = () => {
    setLoading(true);
    apiClient
      .get<ApiResponse<Voucher[]>>(ENDPOINTS.VOUCHERS.BASE)
      .then((res) => setVouchers(res.data.data))
      .catch(() => setVouchers([]))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    fetchVouchers();
  }, []);

  const openCreate = () => {
    setEditingVoucher(null);
    setForm(emptyVoucherForm);
    setShowForm(true);
  };

  const openEdit = (voucher: Voucher) => {
    setEditingVoucher(voucher);
    setForm({
      code: voucher.code,
      description: voucher.description ?? '',
      type: voucher.type,
      discountType: voucher.discountType,
      discountValue: voucher.discountValue,
      maxDiscountAmount: voucher.maxDiscountAmount ?? null,
      minOrderValue: voucher.minOrderValue,
      usageLimit: voucher.usageLimit ?? null,
      startAt: voucher.startAt ? voucher.startAt.slice(0, 16) : null,
      endAt: voucher.endAt ? voucher.endAt.slice(0, 16) : null,
      active: voucher.active,
    });
    setShowForm(true);
  };

  const closeForm = () => {
    setShowForm(false);
    setEditingVoucher(null);
  };

  const normalizePayload = (): VoucherFormPayload => ({
    ...form,
    code: form.code.trim().toUpperCase(),
    description: form.description?.trim() || undefined,
    maxDiscountAmount:
      form.maxDiscountAmount && form.maxDiscountAmount > 0
        ? form.maxDiscountAmount
        : null,
    usageLimit: form.usageLimit && form.usageLimit > 0 ? form.usageLimit : null,
    startAt: form.startAt || null,
    endAt: form.endAt || null,
  });

  const handleSave = async () => {
    const payload = normalizePayload();
    if (!payload.code) {
      addToast('error', 'Mã voucher không được để trống');
      return;
    }

    setSaving(true);
    try {
      if (editingVoucher) {
        await apiClient.put(ENDPOINTS.VOUCHERS.BY_ID(editingVoucher.id), payload);
      } else {
        await apiClient.post(ENDPOINTS.VOUCHERS.BASE, payload);
      }
      closeForm();
      fetchVouchers();
      addToast(
        'success',
        editingVoucher ? 'Đã cập nhật voucher' : 'Đã tạo voucher mới',
      );
    } catch (err: unknown) {
      const axiosErr = err as ApiError;
      addToast(
        'error',
        axiosErr.response?.data?.message ?? 'Không thể lưu voucher',
      );
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (voucher: Voucher) => {
    if (!window.confirm(`Xóa voucher ${voucher.code}?`)) return;

    try {
      await apiClient.delete(ENDPOINTS.VOUCHERS.BY_ID(voucher.id));
      fetchVouchers();
      addToast('success', 'Đã xóa voucher');
    } catch (err: unknown) {
      const axiosErr = err as ApiError;
      addToast(
        'error',
        axiosErr.response?.data?.message ?? 'Không thể xóa voucher',
      );
    }
  };

  return (
    <>
      <div className="rounded-2xl bg-white shadow-sm ring-1 ring-gray-100 overflow-hidden">
        <div className="flex items-center justify-between border-b border-gray-100 px-6 py-5">
          <div>
            <h2 className="font-bold text-gray-800">Quản lý voucher</h2>
            <p className="mt-0.5 text-xs text-gray-400">
              {vouchers.length} voucher
            </p>
          </div>
          <button
            type="button"
            onClick={openCreate}
            className="inline-flex cursor-pointer items-center gap-2 rounded-xl bg-gradient-to-r from-purple-500 to-indigo-600 px-4 py-2.5 text-sm font-semibold text-white shadow-md shadow-purple-200"
          >
            <Plus className="h-4 w-4" />
            Thêm voucher
          </button>
        </div>

        {loading ? (
          <div className="flex items-center justify-center py-20">
            <div className="h-8 w-8 animate-spin rounded-full border-4 border-purple-200 border-t-purple-600" />
          </div>
        ) : vouchers.length === 0 ? (
          <div className="flex flex-col items-center justify-center py-20 text-gray-400">
            <TicketPercent className="h-12 w-12 text-gray-200" />
            <p className="mt-3 text-sm">Chưa có voucher nào</p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="bg-gray-50 text-left text-xs font-semibold uppercase tracking-wider text-gray-400">
                  <th className="px-5 py-4">Mã</th>
                  <th className="px-5 py-4">Loại</th>
                  <th className="px-5 py-4">Giảm giá</th>
                  <th className="px-5 py-4">Điều kiện</th>
                  <th className="px-5 py-4">Hiệu lực</th>
                  <th className="px-5 py-4">Trạng thái</th>
                  <th className="px-5 py-4">Thao tác</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-50">
                {vouchers.map((voucher) => (
                  <tr key={voucher.id} className="hover:bg-gray-50/50">
                    <td className="px-5 py-4">
                      <p className="font-semibold text-gray-800">{voucher.code}</p>
                      <p className="text-xs text-gray-400">
                        {voucher.description || 'Không có mô tả'}
                      </p>
                    </td>
                    <td className="px-5 py-4 text-gray-600">
                      {typeLabel[voucher.type]}
                    </td>
                    <td className="px-5 py-4 text-gray-600">
                      {voucher.discountType === 'PERCENTAGE'
                        ? `${voucher.discountValue}%`
                        : `${voucher.discountValue.toLocaleString('vi-VN')}₫`}
                      {voucher.maxDiscountAmount ? (
                        <p className="text-xs text-gray-400">
                          Tối đa {voucher.maxDiscountAmount.toLocaleString('vi-VN')}₫
                        </p>
                      ) : null}
                    </td>
                    <td className="px-5 py-4 text-gray-600">
                      <p>
                        Tối thiểu {voucher.minOrderValue.toLocaleString('vi-VN')}₫
                      </p>
                      <p className="text-xs text-gray-400">
                        Đã dùng {voucher.usedCount}/
                        {voucher.usageLimit ?? '∞'}
                      </p>
                    </td>
                    <td className="px-5 py-4 text-gray-600">
                      <p>
                        {voucher.startAt
                          ? new Date(voucher.startAt).toLocaleString('vi-VN')
                          : 'Ngay'}
                      </p>
                      <p className="text-xs text-gray-400">
                        {voucher.endAt
                          ? `Đến ${new Date(voucher.endAt).toLocaleString('vi-VN')}`
                          : 'Không giới hạn'}
                      </p>
                    </td>
                    <td className="px-5 py-4">
                      <div className="space-y-1">
                        <span
                          className={`inline-flex rounded-full px-2.5 py-1 text-xs font-semibold ${
                            voucher.active
                              ? 'bg-green-50 text-green-700'
                              : 'bg-gray-100 text-gray-500'
                          }`}
                        >
                          {voucher.active ? 'Đang bật' : 'Đang tắt'}
                        </span>
                        <p
                          className={`text-xs ${
                            voucher.usable ? 'text-green-600' : 'text-orange-500'
                          }`}
                        >
                          {voucher.usable ? 'Có thể dùng' : 'Chưa khả dụng'}
                        </p>
                      </div>
                    </td>
                    <td className="px-5 py-4">
                      <div className="flex items-center gap-2">
                        <button
                          type="button"
                          onClick={() => openEdit(voucher)}
                          className="cursor-pointer rounded-lg p-2 text-indigo-500 hover:bg-indigo-50"
                          title="Sửa"
                        >
                          <Pencil className="h-4 w-4" />
                        </button>
                        <button
                          type="button"
                          onClick={() => handleDelete(voucher)}
                          className="cursor-pointer rounded-lg p-2 text-red-500 hover:bg-red-50"
                          title="Xóa"
                        >
                          <Trash2 className="h-4 w-4" />
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

      {showForm && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4 backdrop-blur-sm">
          <div className="w-full max-w-2xl rounded-2xl bg-white shadow-2xl">
            <div className="flex items-center justify-between border-b border-gray-100 px-6 py-4">
              <div>
                <h3 className="font-bold text-gray-800">
                  {editingVoucher ? 'Cập nhật voucher' : 'Tạo voucher mới'}
                </h3>
                <p className="text-xs text-gray-400">
                  Cấu hình voucher cho user và admin checkout
                </p>
              </div>
              <button
                type="button"
                onClick={closeForm}
                className="cursor-pointer rounded-lg p-1.5 text-gray-400 hover:bg-gray-100"
              >
                <X className="h-5 w-5" />
              </button>
            </div>

            <div className="grid gap-4 px-6 py-5 sm:grid-cols-2">
              <div className="sm:col-span-2">
                <label className="mb-1 block text-xs font-semibold text-gray-500">
                  Mã voucher
                </label>
                <input
                  type="text"
                  value={form.code}
                  onChange={(e) =>
                    setForm((prev) => ({
                      ...prev,
                      code: e.target.value.toUpperCase(),
                    }))
                  }
                  placeholder="VD: SALE10"
                  className={inputClass}
                />
              </div>

              <div className="sm:col-span-2">
                <label className="mb-1 block text-xs font-semibold text-gray-500">
                  Mô tả
                </label>
                <input
                  type="text"
                  value={form.description ?? ''}
                  onChange={(e) =>
                    setForm((prev) => ({ ...prev, description: e.target.value }))
                  }
                  placeholder="Voucher khuyến mãi cuối tuần"
                  className={inputClass}
                />
              </div>

              <div>
                <label className="mb-1 block text-xs font-semibold text-gray-500">
                  Loại voucher
                </label>
                <select
                  value={form.type}
                  onChange={(e) =>
                    setForm((prev) => ({
                      ...prev,
                      type: e.target.value as VoucherType,
                    }))
                  }
                  className={inputClass}
                >
                  <option value="PRODUCT">Giảm sản phẩm</option>
                  <option value="SHIPPING">Freeship</option>
                </select>
              </div>

              <div>
                <label className="mb-1 block text-xs font-semibold text-gray-500">
                  Kiểu giảm
                </label>
                <select
                  value={form.discountType}
                  onChange={(e) =>
                    setForm((prev) => ({
                      ...prev,
                      discountType: e.target.value as VoucherDiscountType,
                    }))
                  }
                  className={inputClass}
                >
                  {Object.entries(discountTypeLabel).map(([value, label]) => (
                    <option key={value} value={value}>
                      {label}
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label className="mb-1 block text-xs font-semibold text-gray-500">
                  Giá trị giảm
                </label>
                <input
                  type="number"
                  min="0"
                  value={form.discountValue || ''}
                  onChange={(e) =>
                    setForm((prev) => ({
                      ...prev,
                      discountValue: e.target.value ? Number(e.target.value) : 0,
                    }))
                  }
                  className={inputClass}
                />
              </div>

              <div>
                <label className="mb-1 block text-xs font-semibold text-gray-500">
                  Giảm tối đa
                </label>
                <input
                  type="number"
                  min="0"
                  value={form.maxDiscountAmount ?? ''}
                  onChange={(e) =>
                    setForm((prev) => ({
                      ...prev,
                      maxDiscountAmount: e.target.value
                        ? Number(e.target.value)
                        : null,
                    }))
                  }
                  placeholder="Bỏ trống nếu không giới hạn"
                  className={inputClass}
                />
              </div>

              <div>
                <label className="mb-1 block text-xs font-semibold text-gray-500">
                  Đơn tối thiểu
                </label>
                <input
                  type="number"
                  min="0"
                  value={form.minOrderValue || ''}
                  onChange={(e) =>
                    setForm((prev) => ({
                      ...prev,
                      minOrderValue: e.target.value ? Number(e.target.value) : 0,
                    }))
                  }
                  className={inputClass}
                />
              </div>

              <div>
                <label className="mb-1 block text-xs font-semibold text-gray-500">
                  Giới hạn lượt dùng
                </label>
                <input
                  type="number"
                  min="0"
                  value={form.usageLimit ?? ''}
                  onChange={(e) =>
                    setForm((prev) => ({
                      ...prev,
                      usageLimit: e.target.value ? Number(e.target.value) : null,
                    }))
                  }
                  placeholder="Bỏ trống nếu không giới hạn"
                  className={inputClass}
                />
              </div>

              <div>
                <label className="mb-1 block text-xs font-semibold text-gray-500">
                  Bắt đầu
                </label>
                <input
                  type="datetime-local"
                  value={form.startAt ?? ''}
                  onChange={(e) =>
                    setForm((prev) => ({
                      ...prev,
                      startAt: e.target.value || null,
                    }))
                  }
                  className={inputClass}
                />
              </div>

              <div>
                <label className="mb-1 block text-xs font-semibold text-gray-500">
                  Kết thúc
                </label>
                <input
                  type="datetime-local"
                  value={form.endAt ?? ''}
                  onChange={(e) =>
                    setForm((prev) => ({
                      ...prev,
                      endAt: e.target.value || null,
                    }))
                  }
                  className={inputClass}
                />
              </div>

              <label className="sm:col-span-2 inline-flex items-center gap-3 rounded-xl border border-gray-200 px-4 py-3 text-sm text-gray-700">
                <input
                  type="checkbox"
                  checked={form.active}
                  onChange={(e) =>
                    setForm((prev) => ({ ...prev, active: e.target.checked }))
                  }
                />
                Bật voucher ngay sau khi lưu
              </label>
            </div>

            <div className="flex gap-3 border-t border-gray-100 px-6 py-4">
              <button
                type="button"
                onClick={closeForm}
                className="flex-1 cursor-pointer rounded-xl border border-gray-200 py-2.5 text-sm font-semibold text-gray-500 hover:bg-gray-50"
              >
                Hủy
              </button>
              <button
                type="button"
                onClick={handleSave}
                disabled={saving}
                className="flex-1 cursor-pointer rounded-xl bg-gradient-to-r from-purple-500 to-indigo-600 py-2.5 text-sm font-semibold text-white shadow-md shadow-purple-200 disabled:opacity-60"
              >
                {saving ? 'Đang lưu...' : editingVoucher ? 'Cập nhật' : 'Tạo mới'}
              </button>
            </div>
          </div>
        </div>
      )}
    </>
  );
}
