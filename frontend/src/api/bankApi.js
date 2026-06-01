import axios from 'axios';

const API = axios.create({
  baseURL: '/api',
  timeout: 10000,
});

// ============================================================
// BRANCH APIs
// ============================================================
export const branchApi = {
  getAll: () => API.get('/branches'),
  getById: (id) => API.get(`/branches/${id}`),
};

// ============================================================
// CUSTOMER APIs
// ============================================================
export const customerApi = {
  getByBranch: (branch) => API.get(`/customers?branch=${branch}`),
  getAll: () => API.get('/customers'),
  getById: (id, branch) => API.get(`/customers/${id}?branch=${branch}`),
  create: (data) => API.post('/customers', data),
  update: (id, data) => API.put(`/customers/${id}`, data),
};

// ============================================================
// ACCOUNT APIs
// ============================================================
export const accountApi = {
  getByBranch: (branch) => API.get(`/accounts?branch=${branch}`),
  getById: (id, branch) => API.get(`/accounts/${id}?branch=${branch}`),
  create: (data) => API.post('/accounts', data),
  getBalance: (id, branch) => API.get(`/accounts/${id}/balance?branch=${branch}`),
  updateStatus: (id, branch, status) =>
    API.put(`/accounts/${id}/status?branch=${branch}&status=${status}`),
  getTransactions: (id, branch) =>
    API.get(`/accounts/${id}/transactions?branch=${branch}`),
};

// ============================================================
// TRANSACTION APIs (Deposit / Withdraw)
// ============================================================
export const transactionApi = {
  deposit: (data) => API.post('/transactions/deposit', data),
  withdraw: (data) => API.post('/transactions/withdraw', data),
  concurrentWithdraw: (data) => API.post('/transactions/concurrent-withdraw', data),
};

// ============================================================
// TRANSFER APIs
// ============================================================
export const transferApi = {
  internal: (data) => API.post('/transfers/internal', data),
  interBranch: (data) => API.post('/transfers/inter-branch', data),
};

// ============================================================
// STATS APIs (Distributed Queries)
// ============================================================
export const statsApi = {
  getTotalBalance: () => API.get('/stats/total-balance'),
  getTopCustomers: (limit = 10) => API.get(`/stats/top-customers?limit=${limit}`),
  getTopDepositingCustomers: (limit = 10) => API.get(`/stats/top-depositing-customers?limit=${limit}`),
  getInterBranchTransactions: () => API.get('/stats/inter-branch-transactions'),
  getIntraBranchTransactions: () => API.get('/stats/intra-branch-transactions'),
  getDepositHistory: (limit = 50) => API.get(`/stats/history/deposit?limit=${limit}`),
  getWithdrawHistory: (limit = 50) => API.get(`/stats/history/withdraw?limit=${limit}`),
  getMultiBranchCustomers: () => API.get('/stats/multi-branch-customers'),
  getTransactionSummary: () => API.get('/stats/transaction-summary'),
};

// ============================================================
// DEMO APIs
// ============================================================
export const demoApi = {
  simulateSiteDown: (branchId, enabled) =>
    API.post('/demo/simulate-site-down', { branchId, enabled }),
  concurrentWithdraw: (branch, accountId, amount) =>
    API.post('/demo/concurrent-withdraw', { branch, accountId, amount }),
  concurrentWithdrawNoLock: (branch, accountId, amount) =>
    API.post('/demo/concurrent-withdraw-no-lock', { branch, accountId, amount }),
  getDistributedTxnLogs: () => API.get('/demo/distributed-txn-logs'),
  deadlock: (data) => API.post('/demo/deadlock', data, { timeout: 30000 }),
};

// ============================================================
// REPLICATION APIs
// ============================================================
export const replicationApi = {
  getStatus: () => API.get('/replication/status'),
  compare: (branchId) => API.get(`/replication/compare/${branchId}`),
  compareAll: () => API.get('/replication/compare-all'),
  demoLag: (branchId) => API.post(`/replication/demo-lag/${branchId}`, {}, { timeout: 15000 }),
};

export default API;
