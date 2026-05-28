import type { Snapshot as SnapshotType } from '../types';

type SnapshotHistoryProps = {
  snapshots: SnapshotType[];
  refreshing: boolean;
  onRefresh: () => void;
};

export default function SnapshotHistory({ snapshots, refreshing, onRefresh }: SnapshotHistoryProps) {
  return (
    <section className="snapshot-panel">
      <div className="snapshot-header">
        <h2>Snapshots</h2>
        <button onClick={onRefresh} disabled={refreshing} className="btn-outline">
          {refreshing ? 'Refrescando...' : 'Refresh data'}
        </button>
      </div>
      {snapshots.length === 0 ? (
        <p className="snapshot-empty">No hay snapshots disponibles. Presioná "Refresh data" para tomar uno.</p>
      ) : (
        <ul className="snapshot-list">
          {snapshots.map(s => (
            <li key={s.id} className="snapshot-item">
              <span className="snapshot-id">{s.id.slice(0, 8)}…</span>
              <span className="snapshot-date">
                {new Date(s.createdAt).toLocaleString('es-AR', {
                  day: '2-digit', month: '2-digit', year: 'numeric',
                  hour: '2-digit', minute: '2-digit', second: '2-digit',
                })}
              </span>
            </li>
          ))}
        </ul>
      )}
    </section>
  );
}
