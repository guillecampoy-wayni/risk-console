import { useCallback, useEffect, useRef, useState } from 'react';
import {
  DEFAULT_ACCOUNT_STATUSES,
  buildAccountStatusParam,
  toggleAccountStatus,
} from '../accountStatusFilters';
import { fetchRiskReport } from '../api/riskReportApi';
import FilterBar from '../components/FilterBar';
import Header from '../components/Header';
import ReportTable from '../components/ReportTable';
import type { CustomerRiskReport } from '../types';

export default function RiskReportPage() {
  const [rows, setRows] = useState<CustomerRiskReport[]>([]);
  const [userStatus, setUserStatus] = useState('BLOCKED');
  const [selectedAccountStatuses, setSelectedAccountStatuses] = useState<string[]>(DEFAULT_ACCOUNT_STATUSES);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const abortRef = useRef<AbortController | null>(null);

  const load = useCallback(async () => {
    abortRef.current?.abort();
    const controller = new AbortController();
    abortRef.current = controller;

    setLoading(true);
    setError(null);
    try {
      const accountStatus = buildAccountStatusParam(selectedAccountStatuses);
      const data = await fetchRiskReport(
        { userStatus, accountStatus, country: 'ARG', page: 1, pageSize: 50 },
        controller.signal,
      );
      if (!controller.signal.aborted) {
        setRows(data);
      }
    } catch (err) {
      if (err instanceof DOMException && err.name === 'AbortError') return;
      setError(err instanceof Error ? err.message : 'Error desconocido');
    } finally {
      if (!controller.signal.aborted) {
        setLoading(false);
      }
    }
  }, [userStatus, selectedAccountStatuses]);

  useEffect(() => {
    void load();
    return () => abortRef.current?.abort();
  }, [load]);

  return (
    <main className="page">
      <Header />
      <FilterBar
        userStatus={userStatus}
        selectedAccountStatuses={selectedAccountStatuses}
        loading={loading}
        onUserStatusChange={setUserStatus}
        onAccountStatusToggle={status =>
          setSelectedAccountStatuses(current => toggleAccountStatus(current, status))
        }
        onSearch={load}
      />
      <ReportTable rows={rows} loading={loading} error={error} />
    </main>
  );
}
