package com.bank.transaction.service;

import com.bank.account.Account;
import com.bank.account.repository.AccountRepository;
import com.bank.transaction.Transaction;
import com.bank.transaction.repository.TransactionRepository;
import com.bank.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionService 단위 테스트")
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private TransactionService transactionService;

    private User mockUser;
    private Account fromAccount;
    private Account toAccount;

    @BeforeEach
    void setUp() {
        mockUser = User.create("test@bank.com", "password123", "홍길동");
        fromAccount = Account.create(mockUser, "111111111");
        toAccount = Account.create(mockUser, "222222222");
        // JPA를 거치지 않으므로 ReflectionTestUtils로 ID 직접 주입
        ReflectionTestUtils.setField(fromAccount, "id", 1L);
        ReflectionTestUtils.setField(toAccount, "id", 2L);
    }

    // =========================================================
    // deposit
    // =========================================================
    @Nested
    @DisplayName("deposit()")
    class Deposit {

        @Test
        @DisplayName("정상 - 유효한 금액 입금 시 잔액 증가 및 Transaction 반환")
        void deposit_success() {
            given(accountRepository.findById(1L)).willReturn(Optional.of(fromAccount));
            Transaction mockTx = Transaction.createDeposit(fromAccount, 10000L);
            given(transactionRepository.save(any(Transaction.class))).willReturn(mockTx);

            Transaction result = transactionService.deposit(1L, 10000L);

            assertThat(result.getType()).isEqualTo("DEPOSIT");
            assertThat(result.getAmount()).isEqualTo(10000L);
            assertThat(fromAccount.getBalance()).isEqualTo(10000L);
        }

        @Test
        @DisplayName("예외 - 입금 금액이 0이면 IllegalArgumentException 발생")
        void deposit_zeroAmount_throwsException() {
            assertThatThrownBy(() -> transactionService.deposit(1L, 0L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("1 이상");

            verify(transactionRepository, never()).save(any());
        }

        @Test
        @DisplayName("예외 - 입금 금액이 null이면 IllegalArgumentException 발생")
        void deposit_nullAmount_throwsException() {
            assertThatThrownBy(() -> transactionService.deposit(1L, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("1 이상");

            verify(transactionRepository, never()).save(any());
        }

        @Test
        @DisplayName("예외 - 존재하지 않는 계좌에 입금 시 IllegalArgumentException 발생")
        void deposit_accountNotFound_throwsException() {
            given(accountRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> transactionService.deposit(99L, 10000L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("존재하지 않는 계좌");

            verify(transactionRepository, never()).save(any());
        }
    }

    // =========================================================
    // withdraw
    // =========================================================
    @Nested
    @DisplayName("withdraw()")
    class Withdraw {

        @BeforeEach
        void depositFirst() {
            fromAccount.increaseBalance(50000L);
        }

        @Test
        @DisplayName("정상 - 잔액 이내 금액 출금 성공")
        void withdraw_success() {
            given(accountRepository.findById(1L)).willReturn(Optional.of(fromAccount));
            Transaction mockTx = Transaction.createWithdraw(fromAccount, 20000L);
            given(transactionRepository.save(any(Transaction.class))).willReturn(mockTx);

            Transaction result = transactionService.withdraw(1L, 20000L);

            assertThat(result.getType()).isEqualTo("WITHDRAW");
            assertThat(result.getAmount()).isEqualTo(20000L);
            assertThat(fromAccount.getBalance()).isEqualTo(30000L);
        }

        @Test
        @DisplayName("예외 - 잔액 부족 시 IllegalArgumentException 발생")
        void withdraw_insufficientBalance_throwsException() {
            given(accountRepository.findById(1L)).willReturn(Optional.of(fromAccount));

            assertThatThrownBy(() -> transactionService.withdraw(1L, 100000L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("잔액이 부족");

            verify(transactionRepository, never()).save(any());
        }

        @Test
        @DisplayName("예외 - 출금 금액이 0이면 IllegalArgumentException 발생")
        void withdraw_zeroAmount_throwsException() {
            assertThatThrownBy(() -> transactionService.withdraw(1L, 0L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("1 이상");

            verify(transactionRepository, never()).save(any());
        }

        @Test
        @DisplayName("예외 - 출금 금액이 음수면 IllegalArgumentException 발생")
        void withdraw_negativeAmount_throwsException() {
            assertThatThrownBy(() -> transactionService.withdraw(1L, -1000L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("1 이상");

            verify(transactionRepository, never()).save(any());
        }

        @Test
        @DisplayName("예외 - 존재하지 않는 계좌에서 출금 시 IllegalArgumentException 발생")
        void withdraw_accountNotFound_throwsException() {
            given(accountRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> transactionService.withdraw(99L, 10000L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("존재하지 않는 계좌");

            verify(transactionRepository, never()).save(any());
        }
    }

    // =========================================================
    // transfer
    // =========================================================
    @Nested
    @DisplayName("transfer()")
    class Transfer {

        @BeforeEach
        void depositFirst() {
            fromAccount.increaseBalance(100000L);
        }

        @Test
        @DisplayName("정상 - 잔액 이내 금액 이체 성공 및 양쪽 잔액 반영")
        void transfer_success() {
            given(accountRepository.findById(1L)).willReturn(Optional.of(fromAccount));
            given(accountRepository.findByAccountNumber("222222222")).willReturn(Optional.of(toAccount));
            Transaction mockTx = Transaction.createTransfer(fromAccount, toAccount, 30000L);
            given(transactionRepository.save(any(Transaction.class))).willReturn(mockTx);

            Transaction result = transactionService.transfer(1L, "222222222", 30000L);

            assertThat(result.getType()).isEqualTo("TRANSFER");
            assertThat(result.getAmount()).isEqualTo(30000L);
            assertThat(fromAccount.getBalance()).isEqualTo(70000L);
            assertThat(toAccount.getBalance()).isEqualTo(30000L);
        }

        @Test
        @DisplayName("예외 - 이체 금액이 0이면 IllegalArgumentException 발생")
        void transfer_zeroAmount_throwsException() {
            assertThatThrownBy(() -> transactionService.transfer(1L, "222222222", 0L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("1 이상");

            verify(transactionRepository, never()).save(any());
        }

        @Test
        @DisplayName("예외 - fromAccountId가 null이면 IllegalArgumentException 발생")
        void transfer_nullFromAccountId_throwsException() {
            // null 체크가 amount 검증 이후에 오므로 amount를 유효값으로 설정
            // 서비스 코드: amount 검증 → null/blank 검증 → findById 순서
            assertThatThrownBy(() -> transactionService.transfer(null, "222222222", 10000L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("필수");

            verify(transactionRepository, never()).save(any());
        }

        @Test
        @DisplayName("예외 - toAccountNumber가 공백이면 IllegalArgumentException 발생")
        void transfer_blankToAccountNumber_throwsException() {
            assertThatThrownBy(() -> transactionService.transfer(1L, "  ", 10000L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("필수");

            verify(transactionRepository, never()).save(any());
        }

        @Test
        @DisplayName("예외 - 존재하지 않는 출금 계좌면 IllegalArgumentException 발생")
        void transfer_fromAccountNotFound_throwsException() {
            given(accountRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> transactionService.transfer(99L, "222222222", 10000L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("출금 계좌가 존재하지 않습니다");

            verify(transactionRepository, never()).save(any());
        }

        @Test
        @DisplayName("예외 - 존재하지 않는 입금 계좌번호면 IllegalArgumentException 발생")
        void transfer_toAccountNotFound_throwsException() {
            given(accountRepository.findById(1L)).willReturn(Optional.of(fromAccount));
            given(accountRepository.findByAccountNumber("000000000")).willReturn(Optional.empty());

            assertThatThrownBy(() -> transactionService.transfer(1L, "000000000", 10000L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("입금 계좌번호를 찾을 수 없습니다");

            verify(transactionRepository, never()).save(any());
        }

        @Test
        @DisplayName("예외 - 동일 계좌로 이체 시 IllegalArgumentException 발생")
        void transfer_sameAccount_throwsException() {
            // fromAccount와 같은 accountNumber로 조회되는 경우 (ID가 동일한 객체)
            given(accountRepository.findById(1L)).willReturn(Optional.of(fromAccount));
            given(accountRepository.findByAccountNumber("111111111")).willReturn(Optional.of(fromAccount));

            assertThatThrownBy(() -> transactionService.transfer(1L, "111111111", 10000L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("동일 계좌로 이체할 수 없습니다");

            verify(transactionRepository, never()).save(any());
        }

        @Test
        @DisplayName("예외 - 잔액 부족 시 IllegalArgumentException 발생")
        void transfer_insufficientBalance_throwsException() {
            given(accountRepository.findById(1L)).willReturn(Optional.of(fromAccount));
            given(accountRepository.findByAccountNumber("222222222")).willReturn(Optional.of(toAccount));

            assertThatThrownBy(() -> transactionService.transfer(1L, "222222222", 200000L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("잔액이 부족");

            verify(transactionRepository, never()).save(any());
        }
    }

    // =========================================================
    // getTransactionsByAccountId
    // =========================================================
    @Nested
    @DisplayName("getTransactionsByAccountId()")
    class GetTransactions {

        @Test
        @DisplayName("정상 - 거래 내역 목록 반환")
        void getTransactions_success() {
            Transaction tx1 = Transaction.createDeposit(fromAccount, 10000L);
            Transaction tx2 = Transaction.createWithdraw(fromAccount, 5000L);
            given(transactionRepository.findAllByAccountIdOrderByLatest(1L)).willReturn(List.of(tx1, tx2));

            List<Transaction> result = transactionService.getTransactionsByAccountId(1L);

            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("정상 - 거래 내역이 없으면 빈 리스트 반환")
        void getTransactions_empty() {
            given(transactionRepository.findAllByAccountIdOrderByLatest(1L)).willReturn(List.of());

            List<Transaction> result = transactionService.getTransactionsByAccountId(1L);

            assertThat(result).isEmpty();
        }
    }
}
