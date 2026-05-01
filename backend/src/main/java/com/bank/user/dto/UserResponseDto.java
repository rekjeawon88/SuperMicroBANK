package com.bank.user.dto;

import com.bank.user.User;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

public class UserResponseDto {

    @Schema(description = "회원가입 응답")
    public record UserResponse(
            @Schema(description = "사용자 ID", example = "1")
            Long id,

            @Schema(description = "사용자 이메일", example = "user@bank.com")
            String email,

            @Schema(description = "사용자 이름", example = "홍길동")
            String name,

            @Schema(description = "가입 일시", example = "2025-01-01T09:00:00")
            LocalDateTime createdAt
    ) {
        public static UserResponse from(User user) {
            return new UserResponse(user.getId(), user.getEmail(), user.getName(), user.getCreatedAt());
        }
    }

    @Schema(description = "로그인 응답")
    public record LoginResponse(
            @Schema(description = "사용자 ID", example = "1")
            Long id,

            @Schema(description = "사용자 이메일", example = "user@bank.com")
            String email,

            @Schema(description = "사용자 이름", example = "홍길동")
            String name,

            @Schema(description = "토큰 타입 (인증 고도화 전까지 NONE 고정)", example = "NONE")
            String tokenType
    ) {
        public static LoginResponse from(User user) {
            return new LoginResponse(user.getId(), user.getEmail(), user.getName(), "NONE");
        }
    }
}