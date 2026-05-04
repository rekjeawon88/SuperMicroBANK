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

            @Schema(description = "계좌번호", example = "123456789")
            String accountNumber,

            @Schema(description = "잔액 (원 단위)", example = "100000")
            Long balance,

            @Schema(description = "계좌 생성 일시", example = "2025-01-01T09:00:00")
            LocalDateTime createdAt
    ) {
        public static AccountDetailResponse from(Account account) {
            // getUser()는 LAZY 로딩 대상이므로, 트랜잭션 내에서 호출되어야 한다.
            // Controller에서 @Transactional 범위 안에 있으므로 안전하다.
            // 단, userId를 직접 column으로 관리하는 방향으로 리팩터링하면 더 안전하다.
            Long userId = (account.getUser() != null) ? account.getUser().getId() : null;
            return new AccountDetailResponse(
                    account.getId(),
                    userId,
                    account.getAccountNumber(),
                    account.getBalance(),
                    account.getCreatedAt()
            );
        }
    }
}
