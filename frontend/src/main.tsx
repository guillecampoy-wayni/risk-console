import React, { useEffect, useState } from 'react';
import { createRoot } from 'react-dom/client';
import './styles.css';

type AccountRiskDetail = {
  accountId: string;
  country: string;
  currency: string;
  balance: string;
  accountStatus: string;
  statusUpdateMotive?: string;
  statusUpdateComment?: string;
  statusUpdatedBy?: string;
  updatedAt?: string;
};

type CustomerRiskReport = {
  userId: string;
  externalId?: string;
  fullName: string;
  email?: string;
  identification?: string;
  taxIdentification?: string;
  userStatus: string;
  accounts: AccountRiskDetail[];
};

const apiBaseUrl = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080';
const internalApiKey = import.meta.env.VITE_INTERNAL_API_KEY ?? 'dev-local-key';

function App() {
  const [rows, setRows] = useState<CustomerRiskReport[]>([]);
  const [userStatus, setUserStatus] = useState('BLOCKED');
  const [accountStatus, setAccountStatus] = useState('ACTIVE,FROZEN,DISABLED,DELETED');
  const [loading, setLoading] = useState(false);

  async function load() {
    setLoading(true);
    try {
      const params = new URLSearchParams({ userStatus, accountStatus, country: 'ARG', page: '1', pageSize: '50' });
      const response = await fetch(`${apiBaseUrl}/api/risk/customers?${params.toString()}`, {
        headers: { 'X-Internal-Api-Key': internalApiKey }
      });
      if (!response.ok) throw new Error(`API error ${response.status}`);
      setRows(await response.json());
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => { void load(); }, []);

  return (
    <main className="page">
      <header>
        <h1>Risk Console</h1>
        <p>Consolidación de usuarios y cuentas Pomelo para análisis de riesgo/fraude.</p>
      </header>

      <section className="filters">
        <label>
          Usuario
          <select value={userStatus} onChange={e => setUserStatus(e.target.value)}>
            <option value="BLOCKED">BLOCKED</option>
            <option value="ACTIVE">ACTIVE</option>
            <option value="ACTIVE,BLOCKED">ACTIVE + BLOCKED</option>
          </select>
        </label>
        <label>
          Cuentas
          <input value={accountStatus} onChange={e => setAccountStatus(e.target.value)} />
        </label>
        <button onClick={() => void load()} disabled={loading}>{loading ? 'Cargando...' : 'Buscar'}</button>
      </section>

      <section className="summary">Clientes encontrados: {rows.length}</section>

      <table>
        <thead>
          <tr>
            <th>Cliente</th>
            <th>Identificación</th>
            <th>Estado usuario</th>
            <th>Cuentas</th>
            <th>Motivos</th>
          </tr>
        </thead>
        <tbody>
          {rows.map(row => (
            <tr key={row.userId}>
              <td><strong>{row.fullName || row.userId}</strong><br />{row.email}<br />{row.externalId}</td>
              <td>{row.identification}<br />{row.taxIdentification}</td>
              <td><span className="badge">{row.userStatus}</span></td>
              <td>{row.accounts.map(a => <div key={a.accountId}>{a.accountId}: {a.accountStatus} / {a.currency} {a.balance}</div>)}</td>
              <td>{row.accounts.map(a => <div key={a.accountId}>{a.statusUpdateMotive} - {a.statusUpdateComment}</div>)}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </main>
  );
}

createRoot(document.getElementById('root')!).render(<App />);
