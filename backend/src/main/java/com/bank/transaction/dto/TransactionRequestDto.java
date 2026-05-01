package com.bank.transaction.dto;

public class TransactionRequestDto {

    public record AmountRequest(Long amount) {
    }

    public record TransferRequest(Long fromAccountId, Long toAccountId, Long amount) {
    }
}
