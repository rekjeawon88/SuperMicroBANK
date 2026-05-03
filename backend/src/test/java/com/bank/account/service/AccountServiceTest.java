package com.bank.account.service;

import com.bank.account.Account;
import com.bank.account.repository.AccountRepository;
import com.bank.user.User;
import com.bank.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("AccountService 단위 테스트")
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AccountService accountService;

    private User mockUser;
    private Account mockAccount;

    @BeforeEach
    void setUp() {
        mockUser = User.create("test@bank.com", "password123", "홍길동");
        mockAccount = Account.create(mockUser, "123456789");
    }

    // =========================================================
    // createAccount
    // =========================================================
    @Nested
    @DisplayName("createAccount()")
    class CreateAccount {

        @Test
        @DisplayName("정상 - 유효한 userId로 계좌 생성 성공")
        void createAccount_success() {
            given(userRepository.findById(1L)).willReturn(Optional.of(mockUser));
            given(accountRepository.findByAccountNumber(any())).willReturn(Optional.empty());
            given(accountRepository.save(any(Account.class))).willReturn(mockAccount);

            Account result = accountService.createAccount(1L);

            assertThat(result).isNotNull();
            assertThat(result.getAccountNumber()).isEqualTo("123456789");
            assertThat(result.getBalance()).isEqualTo(0L);
            verify(accountRepository).save(any(Account.class));
        }

        @Test
        @DisplayName("예외 - 존재하지 않는 userId로 계좌 생성 시 IllegalArgumentException 발생")
        void createAccount_userNotFound_throwsException() {
            given(userRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> accountService.createAccount(99L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("존재하지 않는 사용자");

            verify(accountRepository, never()).save(any());
        }
    }

    // =========================================================
    // getAccountById
    // =========================================================
    @Nested
    @DisplayName("getAccountById()")
    class GetAccountById {

        @Test
        @DisplayName("정상 - 존재하는 계좌 ID로 조회 성공")
        void getAccountById_success() {
            given(accountRepository.findById(1L)).willReturn(Optional.of(mockAccount));

            Account result = accountService.getAccountById(1L);

            assertThat(result.getAccountNumber()).isEqualTo("123456789");
        }

        @Test
        @DisplayName("예외 - 존재하지 않는 계좌 ID 조회 시 IllegalArgumentException 발생")
        void getAccountById_notFound_throwsException() {
            given(accountRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> accountService.getAccountById(99L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("존재하지 않는 계좌");
        }
    }

    // =========================================================
    // getAccountsByUserId
    // =========================================================
    @Nested
    @DisplayName("getAccountsByUserId()")
    class GetAccountsByUserId {

        @Test
        @DisplayName("정상 - userId로 계좌 목록 반환")
        void getAccountsByUserId_success() {
            Account anotherAccount = Account.create(mockUser, "987654321");
            given(accountRepository.findByUserId(1L)).willReturn(List.of(mockAccount, anotherAccount));

            List<Account> result = accountService.getAccountsByUserId(1L);

            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("정상 - 계좌가 없는 userId는 빈 리스트 반환")
        void getAccountsByUserId_empty() {
            given(accountRepository.findByUserId(1L)).willReturn(List.of());

            List<Account> result = accountService.getAccountsByUserId(1L);

            assertThat(result).isEmpty();
        }
    }
}
