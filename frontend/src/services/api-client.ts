import type { ApiResponse, User, Account, Transaction } from './types';

const BASE_URL = 'http://localhost:8080';

async function handleResponse<T>(response: Response): Promise<ApiResponse<T>> {
  const status = response.status;
  try {
    const data = await response.json();
    if (!response.ok) {
      return { error: data.message || 'API Error', status };
    }
    return { data, status };
  } catch (e) {
    if (response.ok) return { status } as ApiResponse<T>;
    return { error: 'Unknown server error', status };
  }
}

export const bankApi = {
  createAccount: (userId: number) =>
    fetch(`${BASE_URL}/accounts`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ userId }),
    }).then(r => handleResponse<Account>(r)),

  getUserAccounts: (userId: number) =>
    fetch(`${BASE_URL}/users/${userId}/accounts`).then(r => handleResponse<Account[]>(r)),

  getAccount: (accountId: number) =>
    fetch(`${BASE_URL}/accounts/${accountId}`).then(r => handleResponse<Account>(r)),

  signup: (userData: any) =>
    fetch(`${BASE_URL}/users`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(userData),
    }).then(r => handleResponse<User>(r)),

  login: (credentials: any) =>
    fetch(`${BASE_URL}/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(credentials),
    }).then(r => handleResponse<User>(r)),

  transfer: (data: { fromAccountId: number; toAccountNumber: string; amount: number }) =>
    fetch(`${BASE_URL}/transfer`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data),
    }).then(r => handleResponse<Transaction>(r)),

  withdraw: (accountId: number, amount: number) =>
    fetch(`${BASE_URL}/accounts/${accountId}/withdraw`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ amount }),
    }).then(r => handleResponse<Account>(r)),

  deposit: (accountId: number, amount: number) =>
    fetch(`${BASE_URL}/accounts/${accountId}/deposit`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ amount }),
    }).then(r => handleResponse<Account>(r)),

  getTransactions: (accountId: number) =>
    fetch(`${BASE_URL}/accounts/${accountId}/transactions`).then(r => handleResponse<Transaction[]>(r)),
};
