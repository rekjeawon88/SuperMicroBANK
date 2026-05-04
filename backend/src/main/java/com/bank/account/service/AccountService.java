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
    private static final int MAX_ACCOUNT_NUMBER_RETRY_COUNT = 20;

    public long getAccountCount() {
        return accountRepository.count();
    }

    // [트러블슈팅 3] @Transactional(readOnly = true) 명시
    // 클래스 레벨에 readOnly = true가 선언되어 있어도,
    // 메서드 레벨에 명시하지 않으면 읽기 전용 의도가 코드에서 드러나지 않음.
    // readOnly = true는 Hibernate의 dirty checking(변경 감지)을 비활성화하여
    // flush를 생략하고 스냅샷을 저장하지 않으므로 조회 성능이 개선됨.
    @Transactional(readOnly = true)
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

    @Transactional(readOnly = true)
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
            // [변경 후] existsByAccountNumber()로 EXISTS 쿼리 실행 — 엔티티 전체 조회 없이 존재 여부만 확인
            if (!accountRepository.existsByAccountNumber(candidateAccountNumber)) {
                return candidateAccountNumber;
            }
        }
        throw new IllegalStateException("고유한 계좌번호 생성에 실패했습니다.");
    }

    private String generateNineDigitAccountNumber() {
        int randomNumber = 1 + SECURE_RANDOM.nextInt(999_999_999);
        return String.format("%09d", randomNumber);
    }
}
