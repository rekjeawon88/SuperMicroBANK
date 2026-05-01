package com.bank.transaction.dto;

import com.bank.transaction.Transaction;
import java.time.LocalDateTime;

public class TransactionResponseDto {

    public record TransactionDetailResponse(
            Long id,
            Long fromAccountId,
            Long toAccountId,
            Long amount,
            String type,
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
