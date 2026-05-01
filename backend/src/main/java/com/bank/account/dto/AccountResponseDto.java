package com.bank.account.dto;

import com.bank.account.Account;
import java.time.LocalDateTime;

public class AccountResponseDto {

    public record AccountDetailResponse(
            Long id,
            Long userId,
            String accountNumber,
            Long balance,
            LocalDateTime createdAt
    ) {
        public static AccountDetailResponse from(Account account) {
            return new AccountDetailResponse(
                    account.getId(),
                    account.getUser().getId(),
                    account.getAccountNumber(),
                    account.getBalance(),
                    account.getCreatedAt()
            );
        }
    }
}
