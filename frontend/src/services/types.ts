export interface User {
  id: number;
  email: string;
  name: string;
}

export interface Account {
  id: number;
  userId: number;
  accountNumber: string;
  balance: number;
  createdAt: string;
}

export interface Transaction {
  id: number;
  fromAccountId: number;
  toAccountId: number;
  amount: number;
  type: 'DEPOSIT' | 'WITHDRAW' | 'TRANSFER';
  createdAt: string;
}

export interface ApiResponse<T> {
  data?: T;
  error?: string;
  status: number;
}
