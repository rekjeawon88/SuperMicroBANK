import React, { useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { 
  Building2, 
  UserPlus, 
  LogIn, 
  Wallet, 
  ArrowRightLeft, 
  History, 
  ArrowUpRight, 
  ArrowDownLeft, 
  CreditCard, 
  User as UserIcon, 
  LogOut,
  AlertCircle,
  CheckCircle2,
  Loader2,
  Copy,
  Check
} from 'lucide-react';
import { bankApi } from './services/api-client';
import type { User, Account, Transaction } from './services/types';
import { cn } from '../lib/utils';

type ToastType = { message: string; type: 'success' | 'error' } | null;

export default function App() {
  const [user, setUser] = useState<User | null>(null);
  const [accounts, setAccounts] = useState<Account[]>([]);
  const [selectedAccount, setSelectedAccount] = useState<Account | null>(null);
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [loading, setLoading] = useState(false);
  const [toast, setToast] = useState<ToastType>(null);
  const [copiedId, setCopiedId] = useState<number | null>(null);

  const [authMode, setAuthMode] = useState<'login' | 'signup'>('login');
  const [authForm, setAuthForm] = useState({ email: '', password: '', name: '' });
  const [transferForm, setTransferForm] = useState({ toAccountNumber: '', amount: '' });
  const [operationForm, setOperationForm] = useState({ amount: '' });

  const showToast = (message: string, type: 'success' | 'error') => {
    setToast({ message, type });
    setTimeout(() => setToast(null), 3500);
  };

  // 날짜+시분 포맷 (예: 2026. 5. 3. 14:30)
  const formatDateTime = (dateStr: string) => {
    const d = new Date(dateStr);
    return d.toLocaleDateString('ko-KR') + ' ' +
      String(d.getHours()).padStart(2, '0') + ':' +
      String(d.getMinutes()).padStart(2, '0');
  };

  const copyAccountNumber = async (acc: Account) => {
    try {
      await navigator.clipboard.writeText(acc.accountNumber);
      setCopiedId(acc.id);
      showToast(`계좌번호 ${acc.accountNumber} 복사됨`, 'success');
      setTimeout(() => setCopiedId(null), 2000);
    } catch {
      showToast('복사에 실패했습니다.', 'error');
    }
  };

  const refreshAccounts = async (userId: number) => {
    const res = await bankApi.getUserAccounts(userId);
    if (res.data) setAccounts(res.data);
  };

  const refreshTransactions = async (accountId: number) => {
    const res = await bankApi.getTransactions(accountId);
    if (res.data) setTransactions(res.data);
  };

  const handleAuth = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    try {
      if (authMode === 'signup') {
        const res = await bankApi.signup(authForm);
        if (res.status === 201) {
          showToast('회원가입이 완료되었습니다! 로그인해 주세요.', 'success');
          setAuthMode('login');
          setAuthForm({ email: authForm.email, password: '', name: '' });
        } else {
          showToast(res.error || '회원가입에 실패했습니다.', 'error');
        }
      } else {
        const res = await bankApi.login({ email: authForm.email, password: authForm.password });
        if (res.data) {
          setUser(res.data);
          refreshAccounts(res.data.id);
          showToast(`${res.data.name}님, 환영합니다!`, 'success');
        } else {
          showToast(res.error || '로그인에 실패했습니다.', 'error');
        }
      }
    } catch {
      showToast('서버에 연결할 수 없습니다. 백엔드가 실행 중인지 확인해 주세요.', 'error');
    } finally {
      setLoading(false);
    }
  };

  const createAccount = async () => {
    if (!user) return;
    try {
      const res = await bankApi.createAccount(user.id);
      if (res.status === 201) {
        refreshAccounts(user.id);
        showToast('새 계좌가 생성되었습니다.', 'success');
      } else {
        showToast(res.error || '계좌 생성에 실패했습니다.', 'error');
      }
    } catch {
      showToast('서버 오류가 발생했습니다.', 'error');
    }
  };

  const handleTransfer = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedAccount) return;
    try {
      const res = await bankApi.transfer({
        fromAccountId: selectedAccount.id,
        toAccountNumber: transferForm.toAccountNumber,
        amount: Number(transferForm.amount)
      });
      if (res.status === 200) {
        refreshAccounts(user!.id);
        refreshTransactions(selectedAccount.id);
        setTransferForm({ toAccountNumber: '', amount: '' });
        showToast('이체가 완료되었습니다.', 'success');
      } else {
        showToast(res.error || '이체에 실패했습니다.', 'error');
      }
    } catch {
      showToast('서버 오류가 발생했습니다.', 'error');
    }
  };

  const handleOperation = async (type: 'deposit' | 'withdraw') => {
    if (!selectedAccount) return;
    const amount = Number(operationForm.amount);
    try {
      const res = type === 'deposit'
        ? await bankApi.deposit(selectedAccount.id, amount)
        : await bankApi.withdraw(selectedAccount.id, amount);
      if (res.status === 200) {
        refreshAccounts(user!.id);
        refreshTransactions(selectedAccount.id);
        setOperationForm({ amount: '' });
        showToast(type === 'deposit' ? '입금이 완료되었습니다.' : '출금이 완료되었습니다.', 'success');
      } else {
        showToast(res.error || `${type === 'deposit' ? '입금' : '출금'}에 실패했습니다.`, 'error');
      }
    } catch {
      showToast('서버 오류가 발생했습니다.', 'error');
    }
  };

  const selectAccount = (acc: Account) => {
    setSelectedAccount(acc);
    refreshTransactions(acc.id);
  };

  return (
    <div className="min-h-screen bg-[#f8fafc] text-slate-800 font-sans selection:bg-brand-primary/30 flex flex-col">
      {/* Toast */}
      <AnimatePresence>
        {toast && (
          <motion.div
            initial={{ opacity: 0, y: -20 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -20 }}
            className={cn(
              "fixed top-4 left-1/2 -translate-x-1/2 z-[100] flex items-center gap-2 px-5 py-3 rounded-xl shadow-xl text-sm font-semibold",
              toast.type === 'success' ? "bg-emerald-500 text-white" : "bg-rose-500 text-white"
            )}
          >
            {toast.type === 'success'
              ? <CheckCircle2 className="w-4 h-4 shrink-0" />
              : <AlertCircle className="w-4 h-4 shrink-0" />}
            {toast.message}
          </motion.div>
        )}
      </AnimatePresence>

      {/* Header */}
      <header className="h-16 bg-brand-secondary text-white flex items-center justify-between px-6 shrink-0 shadow-lg z-50">
        <div className="flex items-center gap-3">
          <div className="w-8 h-8 bg-brand-primary rounded flex items-center justify-center font-bold text-lg shadow-sm">SM</div>
          <h1 className="text-xl font-semibold tracking-tight">SuperMicroBANK</h1>
        </div>
        {user ? (
          <div className="flex items-center gap-4">
            <div className="text-right hidden sm:block">
              <p className="text-sm font-semibold text-white">{user.name}</p>
              <p className="text-[10px] text-slate-400 uppercase tracking-wider">{user.email}</p>
            </div>
            <div className="h-8 w-[1px] bg-slate-700 mx-2"></div>
            <button
              onClick={() => { setUser(null); setAccounts([]); setSelectedAccount(null); }}
              className="p-2 hover:bg-slate-700 rounded-lg transition-colors text-slate-400 hover:text-white"
            >
              <LogOut className="w-5 h-5" />
            </button>
          </div>
        ) : null}
      </header>

      <main className="flex-1 flex overflow-hidden">
        {!user ? (
          <div className="flex-1 flex items-center justify-center p-6 bg-slate-50">
            <motion.div
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              className="w-full max-w-md bg-white border border-slate-200 p-8 rounded-2xl shadow-xl space-y-6"
            >
              <div className="text-center space-y-2">
                <div className="w-12 h-12 bg-brand-primary/10 rounded-2xl flex items-center justify-center mx-auto mb-4">
                  <UserIcon className="w-6 h-6 text-brand-primary" />
                </div>
                <h2 className="text-2xl font-bold tracking-tight text-brand-secondary">
                  {authMode === 'login' ? '다시 오신 것을 환영합니다' : '은행 계정 생성'}
                </h2>
                <p className="text-sm text-slate-500">안전하고 빠른 SM뱅킹 시스템</p>
              </div>

              <div className="flex p-1 bg-slate-100 rounded-lg">
                <button
                  onClick={() => setAuthMode('login')}
                  className={cn(
                    "flex-1 py-2 text-xs font-bold rounded-md transition-all",
                    authMode === 'login' ? "bg-white text-brand-primary shadow-sm" : "text-slate-500 hover:text-slate-700"
                  )}
                >로그인</button>
                <button
                  onClick={() => setAuthMode('signup')}
                  className={cn(
                    "flex-1 py-2 text-xs font-bold rounded-md transition-all",
                    authMode === 'signup' ? "bg-white text-brand-primary shadow-sm" : "text-slate-500 hover:text-slate-700"
                  )}
                >회원가입</button>
              </div>

              <form onSubmit={handleAuth} className="space-y-4">
                <AnimatePresence mode="wait">
                  {authMode === 'signup' && (
                    <motion.div
                      key="name-field"
                      initial={{ opacity: 0, height: 0 }}
                      animate={{ opacity: 1, height: 'auto' }}
                      exit={{ opacity: 0, height: 0 }}
                    >
                      <label className="text-[11px] font-bold text-slate-500 uppercase tracking-widest mb-1.5 block">전체 이름</label>
                      <input
                        type="text"
                        placeholder="홍길동"
                        className="w-full bg-slate-50 border border-slate-200 rounded-lg px-4 py-2.5 text-sm focus:ring-1 focus:ring-brand-primary outline-none transition-all"
                        value={authForm.name}
                        onChange={e => setAuthForm({...authForm, name: e.target.value})}
                        required
                      />
                    </motion.div>
                  )}
                </AnimatePresence>
                <div>
                  <label className="text-[11px] font-bold text-slate-500 uppercase tracking-widest mb-1.5 block">이메일 주소</label>
                  <input
                    type="email"
                    placeholder="user@bank.com"
                    className="w-full bg-slate-50 border border-slate-200 rounded-lg px-4 py-2.5 text-sm focus:ring-1 focus:ring-brand-primary outline-none transition-all"
                    value={authForm.email}
                    onChange={e => setAuthForm({...authForm, email: e.target.value})}
                    required
                  />
                </div>
                <div>
                  <label className="text-[11px] font-bold text-slate-500 uppercase tracking-widest mb-1.5 block">비밀번호</label>
                  <input
                    type="password"
                    placeholder="••••••••"
                    className="w-full bg-slate-50 border border-slate-200 rounded-lg px-4 py-2.5 text-sm focus:ring-1 focus:ring-brand-primary outline-none transition-all"
                    value={authForm.password}
                    onChange={e => setAuthForm({...authForm, password: e.target.value})}
                    required
                  />
                </div>
                <button
                  type="submit"
                  disabled={loading}
                  className="w-full bg-brand-primary hover:opacity-90 disabled:opacity-60 text-white py-3.5 rounded-xl font-bold transition-all mt-4 flex items-center justify-center gap-2 shadow-lg shadow-brand-primary/20 active:scale-[0.98]"
                >
                  {loading
                    ? <Loader2 className="w-4 h-4 animate-spin" />
                    : authMode === 'login' ? <LogIn className="w-4 h-4" /> : <UserPlus className="w-4 h-4" />}
                  {loading ? '처리 중...' : authMode === 'login' ? '계좌 접속' : '지금 가입하기'}
                </button>
              </form>
            </motion.div>
          </div>
        ) : (
          <div className="flex flex-1 overflow-hidden">
            {/* Sidebar */}
            <aside className="w-80 bg-white border-r border-slate-200 flex flex-col shrink-0 overflow-hidden">
              <div className="p-5 border-b border-slate-100 flex items-center justify-between bg-slate-50/50">
                <h2 className="text-xs font-bold text-slate-500 uppercase tracking-widest flex items-center gap-2">
                  <Wallet className="w-4 h-4" />
                  내 계좌 목록
                </h2>
                <button
                  onClick={createAccount}
                  className="text-[10px] font-bold text-brand-primary hover:bg-brand-primary/10 px-2 py-1 rounded transition-colors"
                >
                  + 생성
                </button>
              </div>

              <div className="flex-1 overflow-y-auto p-4 space-y-3 scrollbar-hide">
                <AnimatePresence mode="popLayout">
                  {accounts.length === 0 ? (
                    <div className="py-12 border-2 border-dashed border-slate-100 rounded-xl flex flex-col items-center justify-center text-slate-300">
                      <CreditCard className="w-10 h-10 mb-2 opacity-50" />
                      <p className="text-[10px] uppercase font-bold tracking-tighter">계좌가 없습니다</p>
                    </div>
                  ) : (
                    accounts.map((acc) => (
                      <motion.div
                        key={acc.id}
                        layout
                        initial={{ opacity: 0, x: -10 }}
                        animate={{ opacity: 1, x: 0 }}
                        onClick={() => selectAccount(acc)}
                        className={cn(
                          "relative p-4 rounded-xl cursor-pointer transition-all border group",
                          selectedAccount?.id === acc.id
                            ? "bg-white border-brand-primary ring-1 ring-brand-primary shadow-md"
                            : "bg-white border-slate-100 hover:border-slate-300 hover:bg-slate-50"
                        )}
                      >
                        <div className="flex justify-between items-start mb-3">
                          {/* 계좌번호 클릭 시 복사 */}
                          <button
                            onClick={(e) => { e.stopPropagation(); copyAccountNumber(acc); }}
                            className={cn(
                              "flex items-center gap-1.5 text-[11px] font-bold px-2 py-1 rounded-lg transition-all group/copy",
                              selectedAccount?.id === acc.id
                                ? "bg-brand-primary/10 text-brand-primary hover:bg-brand-primary/20"
                                : "bg-slate-100 text-slate-500 hover:bg-slate-200"
                            )}
                            title="클릭하여 계좌번호 복사"
                          >
                            {copiedId === acc.id
                              ? <Check className="w-3 h-3" />
                              : <Copy className="w-3 h-3 opacity-60 group-hover/copy:opacity-100" />}
                            계좌번호 : {acc.accountNumber}
                          </button>
                          <CreditCard className={cn("w-4 h-4 shrink-0", selectedAccount?.id === acc.id ? "text-brand-primary" : "text-slate-300")} />
                        </div>
                        <div className="text-xl font-mono font-bold text-slate-900 tracking-tight">
                          ₩{acc.balance.toLocaleString()}
                        </div>
                      </motion.div>
                    ))
                  )}
                </AnimatePresence>
              </div>
            </aside>

            {/* Content */}
            <div className="flex-1 flex flex-col overflow-hidden bg-[#f8fafc]">
              <div className="flex-1 overflow-y-auto p-8">
                <div className="max-w-5xl mx-auto grid grid-cols-1 xl:grid-cols-2 gap-8">
                  <div className="space-y-8">
                    <section className="bg-white border border-slate-200 rounded-2xl shadow-sm p-6">
                      <h3 className="text-sm font-bold flex items-center gap-2 mb-6 text-slate-700">
                        <span className="w-2 h-4 bg-brand-primary rounded-full"></span>
                        계좌 작업
                      </h3>
                      {!selectedAccount ? (
                        <div className="py-12 flex flex-col items-center justify-center text-slate-300 border border-slate-50 rounded-xl bg-slate-50/50">
                          <ArrowRightLeft className="w-8 h-8 mb-2 opacity-50" />
                          <p className="text-[11px] font-bold uppercase">작업할 계좌를 선택해 주세요</p>
                        </div>
                      ) : (
                        <div className="space-y-8">
                          <div className="space-y-4">
                            <label className="text-[11px] font-bold text-slate-400 uppercase tracking-widest">계좌 이체</label>
                            <form onSubmit={handleTransfer} className="space-y-3">
                              <div className="grid grid-cols-2 gap-3">
                                <input
                                  type="text"
                                  placeholder="받는 계좌번호"
                                  className="px-3 py-2 text-sm border border-slate-200 rounded-lg bg-slate-50 focus:ring-1 focus:ring-brand-primary outline-none"
                                  value={transferForm.toAccountNumber}
                                  onChange={e => setTransferForm({...transferForm, toAccountNumber: e.target.value})}
                                  required
                                />
                                <input
                                  type="number"
                                  placeholder="금액 (₩)"
                                  className="px-3 py-2 text-sm border border-slate-200 rounded-lg bg-slate-50 focus:ring-1 focus:ring-brand-primary outline-none"
                                  value={transferForm.amount}
                                  onChange={e => setTransferForm({...transferForm, amount: e.target.value})}
                                  required
                                />
                              </div>
                              <button className="w-full bg-brand-secondary text-white py-2.5 rounded-lg text-sm font-bold tracking-wide hover:opacity-90 transition-all shadow-md">
                                이체하기
                              </button>
                            </form>
                          </div>

                          <hr className="border-slate-100" />

                          <div className="grid grid-cols-2 gap-8">
                            <div className="space-y-3">
                              <label className="text-[10px] font-bold text-slate-400 uppercase tracking-widest">입금</label>
                              <input
                                type="number"
                                placeholder="금액"
                                className="w-full px-3 py-2 text-sm border border-slate-200 rounded-lg bg-slate-50 outline-none"
                                value={operationForm.amount}
                                onChange={e => setOperationForm({ amount: e.target.value })}
                              />
                              <button
                                onClick={() => handleOperation('deposit')}
                                className="w-full border-2 border-brand-secondary text-brand-secondary py-2 rounded-lg text-[10px] font-bold uppercase hover:bg-brand-secondary hover:text-white transition-all"
                              >입금하기</button>
                            </div>
                            <div className="space-y-3">
                              <label className="text-[10px] font-bold text-slate-400 uppercase tracking-widest">출금</label>
                              <input
                                type="number"
                                placeholder="금액"
                                className="w-full px-3 py-2 text-sm border border-slate-200 rounded-lg bg-slate-50 outline-none"
                                value={operationForm.amount}
                                onChange={e => setOperationForm({ amount: e.target.value })}
                              />
                              <button
                                onClick={() => handleOperation('withdraw')}
                                className="w-full border-2 border-brand-secondary text-brand-secondary py-2 rounded-lg text-[10px] font-bold uppercase hover:bg-brand-secondary hover:text-white transition-all"
                              >출금하기</button>
                            </div>
                          </div>
                        </div>
                      )}
                    </section>
                  </div>

                  {/* Transaction History */}
                  <section className="bg-white border border-slate-200 rounded-2xl shadow-sm p-6 flex flex-col h-full min-h-[600px]">
                    <div className="flex justify-between items-center mb-6">
                      <h3 className="text-sm font-bold flex items-center gap-2 text-slate-700">
                        <span className="w-2 h-4 bg-emerald-500 rounded-full"></span>
                        거래 내역
                      </h3>
                    </div>
                    {!selectedAccount ? (
                      <div className="flex-1 flex flex-col items-center justify-center text-slate-300">
                        <History className="w-12 h-12 mb-4 opacity-20" />
                        <p className="text-xs font-bold uppercase tracking-widest">계좌 선택 대기 중</p>
                      </div>
                    ) : (
                      <div className="flex-1 flex flex-col overflow-hidden">
                        <div className="grow overflow-y-auto rounded-xl border border-slate-100">
                          <table className="w-full text-left border-collapse">
                            <thead className="sticky top-0 z-10 bg-slate-50 text-[10px] font-bold uppercase text-slate-400">
                              <tr>
                                <th className="py-2.5 px-4">유형</th>
                                <th className="py-2.5 px-4">금액</th>
                                <th className="py-2.5 px-4 text-right">날짜</th>
                              </tr>
                            </thead>
                            <tbody className="text-xs divide-y divide-slate-100">
                              <AnimatePresence mode="popLayout">
                                {transactions.length === 0 ? (
                                  <tr>
                                    <td colSpan={3} className="py-12 text-center text-slate-400 italic">기록이 없습니다</td>
                                  </tr>
                                ) : (
                                  transactions.map((tx) => (
                                    <motion.tr
                                      key={tx.id}
                                      initial={{ opacity: 0 }}
                                      animate={{ opacity: 1 }}
                                      className="hover:bg-slate-50/80 transition-colors"
                                    >
                                      <td className="py-3.5 px-4">
                                        <div className="flex items-center gap-3">
                                          <div className={cn(
                                            "w-7 h-7 rounded-lg flex items-center justify-center shrink-0",
                                            tx.type === 'DEPOSIT' || (tx.type === 'TRANSFER' && tx.toAccountId === selectedAccount.id)
                                              ? "bg-emerald-50 text-emerald-600"
                                              : "bg-rose-50 text-rose-600"
                                          )}>
                                            {tx.type === 'DEPOSIT' ? <ArrowDownLeft className="w-4 h-4" /> :
                                             tx.type === 'WITHDRAW' ? <ArrowUpRight className="w-4 h-4" /> :
                                             <ArrowRightLeft className="w-4 h-4" />}
                                          </div>
                                          <div>
                                            <p className="font-bold text-slate-700">
                                              {tx.type === 'DEPOSIT' ? '입금' : tx.type === 'WITHDRAW' ? '출금' : '이체'}
                                            </p>
                                            <p className="text-[9px] text-slate-400">
                                              {tx.type === 'TRANSFER'
                                                ? (tx.fromAccountId === selectedAccount.id ? `#${tx.toAccountId} 계좌로` : `#${tx.fromAccountId} 계좌로부터`)
                                                : "직접 처리"}
                                            </p>
                                          </div>
                                        </div>
                                      </td>
                                      <td className={cn(
                                        "py-3.5 px-4 font-mono font-bold text-sm",
                                        tx.type === 'DEPOSIT' || (tx.type === 'TRANSFER' && tx.toAccountId === selectedAccount.id)
                                          ? "text-emerald-600"
                                          : "text-rose-600"
                                      )}>
                                        {tx.type === 'DEPOSIT' || (tx.type === 'TRANSFER' && tx.toAccountId === selectedAccount.id) ? '+' : '-'}
                                        ₩{tx.amount.toLocaleString()}
                                      </td>
                                      {/* 날짜 + 시:분 */}
                                      <td className="py-3.5 px-4 text-right text-[10px] text-slate-500 font-mono">
                                        {formatDateTime(tx.createdAt)}
                                      </td>
                                    </motion.tr>
                                  ))
                                )}
                              </AnimatePresence>
                            </tbody>
                          </table>
                        </div>
                        <div className="mt-4 pt-4 border-t border-slate-100 flex justify-between items-center text-[10px] text-slate-400 font-medium">
                          <span>총 {transactions.length}개의 기록을 불러왔습니다.</span>
                          <button onClick={() => refreshTransactions(selectedAccount.id)} className="text-brand-primary font-bold hover:underline underline-offset-4">데이터 새로고침</button>
                        </div>
                      </div>
                    )}
                  </section>
                </div>
              </div>
            </div>
          </div>
        )}
      </main>

      <footer className="h-10 bg-slate-100 border-t border-slate-200 flex items-center justify-center px-6 shrink-0 text-[10px] font-bold text-slate-500 uppercase tracking-[0.1em]">
        <div className="flex items-center gap-2">
          <Building2 className="w-3 h-3" />
          <span>© 2026 SuperMicroBANK. All Rights Reserved.</span>
        </div>
      </footer>
    </div>
  );
}
