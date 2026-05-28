import type { CustomerRiskReport } from '../types';

const API_BASE = import.meta.env.VITE_API_BASE_URL ?? '';
const INTERNAL_API_KEY = import.meta.env.VITE_INTERNAL_API_KEY ?? 'dev-local-key';

export type SearchFilters = {
  userStatus: string;
  accountStatus: string;
  country: string;
  page: number;
  pageSize: number;
};

export async function fetchRiskReport(
  filters: SearchFilters,
  signal?: AbortSignal,
): Promise<CustomerRiskReport[]> {
  const params = new URLSearchParams({
    userStatus: filters.userStatus,
    accountStatus: filters.accountStatus,
    country: filters.country,
    page: String(filters.page),
    pageSize: String(filters.pageSize),
  });

  const response = await fetch(`${API_BASE}/api/risk/customers?${params.toString()}`, {
    headers: { 'X-Internal-Api-Key': INTERNAL_API_KEY },
    signal,
  });

  if (!response.ok) {
    throw new Error(`API error ${response.status}`);
  }

  return response.json() as Promise<CustomerRiskReport[]>;
}
