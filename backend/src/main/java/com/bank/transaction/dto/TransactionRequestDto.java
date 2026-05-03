package com.bank.transaction.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public class TransactionRequestDto {

    @Schema(description = "금액 요청 (입금/출금)")
    public record AmountRequest(
            @Schema(description = "거래 금액 (원 단위)", example = "50000")
            Long amount
    ) {}

    @Schema(description = "이체 요청")
    public record TransferRequest(
            @Schema(description = "출금 계좌 ID", example = "1")
            Long fromAccountId,

            @Schema(description = "입금 계좌 번호", example = "012345678")
            String toAccountNumber,

            @Schema(description = "이체 금액 (원 단위)", example = "50000")
            Long amount
    ) {}
}
