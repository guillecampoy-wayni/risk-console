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

export default function ReportTable({ rows, loading, error }: ReportTableProps) {
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
            <tr key={row.userId}>
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
          ))}
        </tbody>
      </table>
    </>
  );
}
