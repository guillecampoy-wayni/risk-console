export const ACCOUNT_STATUS_OPTIONS = ['ACTIVE', 'FROZEN', 'DISABLED', 'DELETED'];

export const DEFAULT_ACCOUNT_STATUSES = [...ACCOUNT_STATUS_OPTIONS];

export function buildAccountStatusParam(selectedStatuses) {
  return ACCOUNT_STATUS_OPTIONS
    .filter(status => selectedStatuses.includes(status))
    .join(',');
}

export function toggleAccountStatus(selectedStatuses, status) {
  if (!ACCOUNT_STATUS_OPTIONS.includes(status)) {
    return [...selectedStatuses];
  }

  if (selectedStatuses.includes(status)) {
    return selectedStatuses.length === 1
      ? [...selectedStatuses]
      : selectedStatuses.filter(selectedStatus => selectedStatus !== status);
  }

  return ACCOUNT_STATUS_OPTIONS.filter(option => [...selectedStatuses, status].includes(option));
}
