package com.bank.transaction.dto;

import com.bank.transaction.Transaction;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

public class TransactionResponseDto {

    @Schema(description = "거래 내역 상세 응답")
    public record TransactionDetailResponse(
            @Schema(description = "거래 ID", example = "1")
            Long id,

            @Schema(description = "출금 계좌 ID (입금 거래 시 null)", example = "1", nullable = true)
            Long fromAccountId,

            @Schema(description = "입금 계좌 ID (출금 거래 시 null)", example = "2", nullable = true)
            Long toAccountId,

            @Schema(description = "거래 금액 (원 단위)", example = "50000")
            Long amount,

            @Schema(description = "거래 유형 (DEPOSIT: 입금, WITHDRAWAL: 출금, TRANSFER: 이체)", example = "DEPOSIT")
            String type,

            @Schema(description = "거래 일시", example = "2025-01-01T09:00:00")
            LocalDateTime createdAt
    ) {
        public static TransactionDetailResponse from(Transaction transaction) {
            return new TransactionDetailResponse(
                    transaction.getId(),
                    transaction.getFromAccount() == null ? null : transaction.getFromAccount().getId(),
                    transaction.getToAccount() == null ? null : transaction.getToAccount().getId(),
                    transaction.getAmount(),
                    transaction.getType(),
                    transaction.getCreatedAt()
            );
        }
    }
}