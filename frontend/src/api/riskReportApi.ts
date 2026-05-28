import type { CustomerRiskReport, Snapshot } from '../types';

const API_BASE = import.meta.env.VITE_API_BASE_URL ?? '';
const INTERNAL_API_KEY = import.meta.env.VITE_INTERNAL_API_KEY ?? 'dev-local-key';

export type SearchFilters = {
  userStatus: string;
  accountStatus: string;
  country: string;
  page: number;
  pageSize: number;
};

function buildUrl(filters: SearchFilters): string {
  const params = new URLSearchParams({
    userStatus: filters.userStatus,
    accountStatus: filters.accountStatus,
    country: filters.country,
    page: String(filters.page),
    pageSize: String(filters.pageSize),
  });
  return `${API_BASE}/api/risk/customers?${params.toString()}`;
}

export async function fetchRiskReport(
  filters: SearchFilters,
  signal?: AbortSignal,
): Promise<CustomerRiskReport[]> {
  const response = await fetch(buildUrl(filters), {
    headers: { 'X-Internal-Api-Key': INTERNAL_API_KEY },
    signal,
  });

  if (!response.ok) {
    throw new Error(`API error ${response.status}`);
  }

  return response.json() as Promise<CustomerRiskReport[]>;
}

export async function createSnapshot(): Promise<Snapshot> {
  const response = await fetch(`${API_BASE}/api/risk/snapshots`, {
    method: 'POST',
    headers: { 'X-Internal-Api-Key': INTERNAL_API_KEY },
  });

  if (!response.ok) {
    throw new Error(`Snapshot error ${response.status}`);
  }

  return response.json() as Promise<Snapshot>;
}

export async function listSnapshots(): Promise<Snapshot[]> {
  const response = await fetch(`${API_BASE}/api/risk/snapshots`, {
    headers: { 'X-Internal-Api-Key': INTERNAL_API_KEY },
  });

  if (!response.ok) {
    throw new Error(`Snapshot list error ${response.status}`);
  }

  return response.json() as Promise<Snapshot[]>;
}

export async function downloadCsv(filters: SearchFilters): Promise<Blob> {
  const response = await fetch(buildUrl(filters), {
    headers: {
      'X-Internal-Api-Key': INTERNAL_API_KEY,
      Accept: 'text/plain',
    },
  });

  if (!response.ok) {
    throw new Error(`CSV export error ${response.status}`);
  }

  return response.blob();
}
