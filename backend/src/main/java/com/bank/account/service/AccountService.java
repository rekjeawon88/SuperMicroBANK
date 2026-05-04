package com.bank.account.service;

import com.bank.account.Account;
import com.bank.account.repository.AccountRepository;
import com.bank.user.User;
import com.bank.user.repository.UserRepository;
import java.security.SecureRandom;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int ACCOUNT_NUMBER_LENGTH = 9;
    private static final int MAX_ACCOUNT_NUMBER_RETRY_COUNT = 20;

    public long getAccountCount() {
        return accountRepository.count();
    }

    public Account getAccountById(Long accountId) {
        try {
            return accountRepository.findById(accountId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 계좌입니다."));
        } catch (IllegalArgumentException illegalArgumentException) {
            throw illegalArgumentException;
        } catch (Exception exception) {
            throw new IllegalStateException("계좌 상세 조회 중 오류가 발생했습니다.", exception);
        }
    }

    public List<Account> getAccountsByUserId(Long userId) {
        try {
            return accountRepository.findByUserId(userId);
        } catch (Exception exception) {
            throw new IllegalStateException("계좌 목록 조회 중 오류가 발생했습니다.", exception);
        }
    }

    @Transactional
    public Account createAccount(Long userId) {
        try {
            User foundUser = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

            String generatedAccountNumber = generateUniqueAccountNumber();
            Account newAccount = Account.create(foundUser, generatedAccountNumber);
            return accountRepository.save(newAccount);
        } catch (IllegalArgumentException illegalArgumentException) {
            throw illegalArgumentException;
        } catch (Exception exception) {
            throw new IllegalStateException("계좌 생성 중 오류가 발생했습니다.", exception);
        }
    }

    private String generateUniqueAccountNumber() {
        for (int retryCount = 0; retryCount < MAX_ACCOUNT_NUMBER_RETRY_COUNT; retryCount++) {
            String candidateAccountNumber = generateNineDigitAccountNumber();
            boolean alreadyExists = accountRepository.findByAccountNumber(candidateAccountNumber).isPresent();
            if (!alreadyExists) {
                return candidateAccountNumber;
            }
        }
        throw new IllegalStateException("고유한 계좌번호 생성에 실패했습니다.");
    }

    private String generateNineDigitAccountNumber() {
        // 1 ~ 999,999,999 범위로 000000000 계좌번호 생성 방지
        int randomNumber = 1 + SECURE_RANDOM.nextInt(999_999_999);
        return String.format("%09d", randomNumber);
    }
}
