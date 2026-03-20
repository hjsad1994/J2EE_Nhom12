const LEGACY_MILLION_THRESHOLD = 1_000;
const MILLION_TO_VND = 1_000_000;

export function normalizeVndAmount(amount: number | null | undefined): number {
  const value = Number(amount ?? 0);
  if (!Number.isFinite(value) || value <= 0) {
    return 0;
  }

  const normalized =
    value < LEGACY_MILLION_THRESHOLD ? value * MILLION_TO_VND : value;
  return Math.round(normalized);
}

export function formatCurrencyVnd(amount: number | null | undefined): string {
  return `${normalizeVndAmount(amount).toLocaleString('vi-VN')}₫`;
}
