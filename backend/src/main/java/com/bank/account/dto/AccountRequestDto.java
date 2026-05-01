package com.bank.account.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public class AccountRequestDto {

    @Schema(description = "계좌 생성 요청")
    public record CreateAccountRequest(
            @Schema(description = "계좌를 생성할 사용자 ID", example = "1")
            Long userId
    ) {}
}