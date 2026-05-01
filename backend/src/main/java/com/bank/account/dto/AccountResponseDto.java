package com.bank.account.dto;

import com.bank.account.Account;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

public class AccountResponseDto {

    @Schema(description = "계좌 상세 응답")
    public record AccountDetailResponse(
            @Schema(description = "계좌 ID", example = "1")
            Long id,

            @Schema(description = "계좌 소유자 사용자 ID", example = "1")
            Long userId,

            @Schema(description = "계좌번호", example = "110-1234-5678")
            String accountNumber,

            @Schema(description = "잔액 (원 단위)", example = "100000")
            Long balance,

            @Schema(description = "계좌 생성 일시", example = "2025-01-01T09:00:00")
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