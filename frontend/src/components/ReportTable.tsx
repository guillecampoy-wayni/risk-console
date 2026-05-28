import { useState } from 'react';
import type { CustomerRiskReport } from '../types';

type ReportTableProps = {
  rows: CustomerRiskReport[];
  loading: boolean;
  error: string | null;
};

function AccountCell({ accounts }: { accounts: CustomerRiskReport['accounts'] }) {
  if (accounts.length === 0) {
    return <em style={{ color: 'var(--color-text-muted)' }}>Sin cuentas</em>;
  }
  return (
    <>
      {accounts.map(a => (
        <div key={a.accountId}>
          {a.accountId}: {a.accountStatus} / {a.currency} {a.balance}
        </div>
      ))}
    </>
  );
}

function MotiveCell({ accounts }: { accounts: CustomerRiskReport['accounts'] }) {
  if (accounts.length === 0) {
    return <em style={{ color: 'var(--color-text-muted)' }}>—</em>;
  }
  return (
    <>
      {accounts.map(a => (
        <div key={a.accountId}>
          {a.statusUpdateMotive ?? '—'}{a.statusUpdateComment ? ` - ${a.statusUpdateComment}` : ''}
        </div>
      ))}
    </>
  );
}

function AccountDetailRow({ account }: { account: CustomerRiskReport['accounts'][number] }) {
  return (
    <tr className="detail-row">
      <td colSpan={5}>
        <div className="account-detail">
          <dl>
            <div>
              <dt>ID</dt>
              <dd>{account.accountId}</dd>
            </div>
            <div>
              <dt>País</dt>
              <dd>{account.country}</dd>
            </div>
            <div>
              <dt>Moneda</dt>
              <dd>{account.currency}</dd>
            </div>
            <div>
              <dt>Saldo</dt>
              <dd>{account.balance}</dd>
            </div>
            <div>
              <dt>Estado</dt>
              <dd><span className="badge">{account.accountStatus}</span></dd>
            </div>
            <div>
              <dt>Motivo</dt>
              <dd>{account.statusUpdateMotive ?? '—'}</dd>
            </div>
            <div>
              <dt>Comentario</dt>
              <dd>{account.statusUpdateComment ?? '—'}</dd>
            </div>
            <div>
              <dt>Actualizado por</dt>
              <dd>{account.statusUpdatedBy ?? '—'}</dd>
            </div>
            <div>
              <dt>Actualizado el</dt>
              <dd>{account.updatedAt ?? '—'}</dd>
            </div>
          </dl>
        </div>
      </td>
    </tr>
  );
}

export default function ReportTable({ rows, loading, error }: ReportTableProps) {
  const [expandedUserId, setExpandedUserId] = useState<string | null>(null);

  if (loading) {
    return <section className="summary">Cargando datos...</section>;
  }

  if (error) {
    return <section className="summary" style={{ color: 'var(--ds-color-orange-500)' }}>{error}</section>;
  }

  if (rows.length === 0) {
    return <section className="summary">No se encontraron clientes con los filtros seleccionados.</section>;
  }

  return (
    <>
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
            <>
              <tr
                key={row.userId}
                className={expandedUserId === row.userId ? 'row-expanded' : ''}
                onClick={() => setExpandedUserId(expandedUserId === row.userId ? null : row.userId)}
                style={{ cursor: 'pointer' }}
              >
                <td>
                  <strong>{row.fullName || row.userId}</strong>
                  <br />
                  {row.email}
                  <br />
                  {row.externalId}
                </td>
                <td>
                  {row.identification}
                  <br />
                  {row.taxIdentification}
                </td>
                <td>
                  <span className="badge">{row.userStatus}</span>
                </td>
                <td><AccountCell accounts={row.accounts} /></td>
                <td><MotiveCell accounts={row.accounts} /></td>
              </tr>
              {expandedUserId === row.userId && row.accounts.map(a => (
                <AccountDetailRow key={a.accountId} account={a} />
              ))}
            </>
          ))}
        </tbody>
      </table>
    </>
  );
}
