export type AccountRiskDetail = {
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

export type CustomerRiskReport = {
  userId: string;
  externalId?: string;
  fullName: string;
  email?: string;
  identification?: string;
  taxIdentification?: string;
  userStatus: string;
  accounts: AccountRiskDetail[];
};

export type Snapshot = {
  id: string;
  createdAt: string;
};
