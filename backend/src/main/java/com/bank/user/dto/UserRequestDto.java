package com.bank.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public class UserRequestDto {

    @Schema(description = "회원가입 요청")
    public record SignUpRequest(
            @Schema(description = "사용자 이메일", example = "user@bank.com")
            String email,

            @Schema(description = "비밀번호", example = "password123!")
            String password,

            @Schema(description = "사용자 이름", example = "홍길동")
            String name
    ) {}

    @Schema(description = "로그인 요청")
    public record LoginRequest(
            @Schema(description = "사용자 이메일", example = "user@bank.com")
            String email,

            @Schema(description = "비밀번호", example = "password123!")
            String password
    ) {}
}