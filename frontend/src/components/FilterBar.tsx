import {
  ACCOUNT_STATUS_OPTIONS,
  toggleAccountStatus,
} from '../accountStatusFilters';

type FilterBarProps = {
  userStatus: string;
  selectedAccountStatuses: string[];
  loading: boolean;
  onUserStatusChange: (value: string) => void;
  onAccountStatusToggle: (status: string) => void;
  onSearch: () => void;
};

export default function FilterBar({
  userStatus,
  selectedAccountStatuses,
  loading,
  onUserStatusChange,
  onAccountStatusToggle,
  onSearch,
}: FilterBarProps) {
  return (
    <section className="filters">
      <label>
        Usuario
        <select value={userStatus} onChange={e => onUserStatusChange(e.target.value)}>
          <option value="BLOCKED">BLOCKED</option>
          <option value="ACTIVE">ACTIVE</option>
          <option value="ACTIVE,BLOCKED">ACTIVE + BLOCKED</option>
        </select>
      </label>

      <fieldset className="account-status-filter">
        <legend>Cuentas</legend>
        <div className="checkbox-group">
          {ACCOUNT_STATUS_OPTIONS.map(status => (
            <label className="checkbox-option" key={status}>
              <input
                type="checkbox"
                checked={selectedAccountStatuses.includes(status)}
                onChange={() => onAccountStatusToggle(status)}
              />
              <span>{status}</span>
            </label>
          ))}
        </div>
      </fieldset>

      <button onClick={onSearch} disabled={loading}>
        {loading ? 'Cargando...' : 'Buscar'}
      </button>
    </section>
  );
}
