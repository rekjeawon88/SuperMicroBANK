package com.bank.transaction.service;

import com.bank.account.Account;
import com.bank.account.repository.AccountRepository;
import com.bank.transaction.Transaction;
import com.bank.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    public long getTransactionCount() {
        return transactionRepository.count();
    }

    @Transactional
    public Transaction deposit(Long accountId, Long amount) {
        try {
            if (amount == null || amount < 1) {
                throw new IllegalArgumentException("입금 금액은 1 이상이어야 합니다.");
            }

            Account foundAccount = accountRepository.findById(accountId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 계좌입니다."));

            foundAccount.increaseBalance(amount);
            Transaction depositTransaction = Transaction.createDeposit(foundAccount, amount);
            return transactionRepository.save(depositTransaction);
        } catch (IllegalArgumentException illegalArgumentException) {
            throw illegalArgumentException;
        } catch (Exception exception) {
            throw new IllegalStateException("입금 처리 중 오류가 발생했습니다.", exception);
        }
    }

    @Transactional
    public Transaction withdraw(Long accountId, Long amount) {
        try {
            if (amount == null || amount < 1) {
                throw new IllegalArgumentException("출금 금액은 1 이상이어야 합니다.");
            }

            Account foundAccount = accountRepository.findById(accountId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 계좌입니다."));

            if (foundAccount.getBalance() < amount) {
                throw new IllegalArgumentException("잔액이 부족합니다.");
            }

            foundAccount.decreaseBalance(amount);
            Transaction withdrawTransaction = Transaction.createWithdraw(foundAccount, amount);
            return transactionRepository.save(withdrawTransaction);
        } catch (IllegalArgumentException illegalArgumentException) {
            throw illegalArgumentException;
        } catch (Exception exception) {
            throw new IllegalStateException("출금 처리 중 오류가 발생했습니다.", exception);
        }
    }

    @Transactional
    public Transaction transfer(Long fromAccountId, Long toAccountId, Long amount) {
        try {
            if (amount == null || amount < 1) {
                throw new IllegalArgumentException("이체 금액은 1 이상이어야 합니다.");
            }

            if (fromAccountId == null || toAccountId == null) {
                throw new IllegalArgumentException("출금 계좌와 입금 계좌는 필수입니다.");
            }

            if (fromAccountId.equals(toAccountId)) {
                throw new IllegalArgumentException("동일 계좌로 이체할 수 없습니다.");
            }

            Account fromAccount = accountRepository.findById(fromAccountId)
                    .orElseThrow(() -> new IllegalArgumentException("출금 계좌가 존재하지 않습니다."));

            Account toAccount = accountRepository.findById(toAccountId)
                    .orElseThrow(() -> new IllegalArgumentException("입금 계좌가 존재하지 않습니다."));

            if (fromAccount.getBalance() < amount) {
                throw new IllegalArgumentException("잔액이 부족합니다.");
            }

            fromAccount.decreaseBalance(amount);
            toAccount.increaseBalance(amount);
            Transaction transferTransaction = Transaction.createTransfer(fromAccount, toAccount, amount);
            return transactionRepository.save(transferTransaction);
        } catch (IllegalArgumentException illegalArgumentException) {
            throw illegalArgumentException;
        } catch (Exception exception) {
            throw new IllegalStateException("계좌 이체 처리 중 오류가 발생했습니다.", exception);
        }
    }
}
