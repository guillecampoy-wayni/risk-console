import { describe, it } from 'node:test';
import assert from 'node:assert/strict';
import {
  ACCOUNT_STATUS_OPTIONS,
  DEFAULT_ACCOUNT_STATUSES,
  buildAccountStatusParam,
  toggleAccountStatus
} from './accountStatusFilters.js';

describe('account status filters', () => {
  it('builds the current backend query contract from the default selection', () => {
    assert.deepEqual(DEFAULT_ACCOUNT_STATUSES, ACCOUNT_STATUS_OPTIONS);
    assert.equal(buildAccountStatusParam(DEFAULT_ACCOUNT_STATUSES), 'ACTIVE,FROZEN,DISABLED,DELETED');
  });

  it('removes an unchecked account status from the query parameter', () => {
    const selectedStatuses = toggleAccountStatus(DEFAULT_ACCOUNT_STATUSES, 'FROZEN');

    assert.deepEqual(selectedStatuses, ['ACTIVE', 'DISABLED', 'DELETED']);
    assert.equal(buildAccountStatusParam(selectedStatuses), 'ACTIVE,DISABLED,DELETED');
  });

  it('keeps query parameter ordering stable when an account status is reselected', () => {
    const withoutActive = toggleAccountStatus(DEFAULT_ACCOUNT_STATUSES, 'ACTIVE');
    const selectedStatuses = toggleAccountStatus(withoutActive, 'ACTIVE');

    assert.deepEqual(selectedStatuses, ['ACTIVE', 'FROZEN', 'DISABLED', 'DELETED']);
    assert.equal(buildAccountStatusParam(selectedStatuses), 'ACTIVE,FROZEN,DISABLED,DELETED');
  });

  it('does not allow removing the last selected account status', () => {
    const selectedStatuses = toggleAccountStatus(['FROZEN'], 'FROZEN');

    assert.deepEqual(selectedStatuses, ['FROZEN']);
    assert.equal(buildAccountStatusParam(selectedStatuses), 'FROZEN');
  });

  it('ignores statuses that are outside the supported account filter set', () => {
    const selectedStatuses = toggleAccountStatus(['ACTIVE'], 'CLOSED');

    assert.deepEqual(selectedStatuses, ['ACTIVE']);
    assert.equal(buildAccountStatusParam(selectedStatuses), 'ACTIVE');
  });
});
