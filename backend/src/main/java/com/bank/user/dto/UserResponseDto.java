package com.bank.user.dto;

import com.bank.user.User;
import java.time.LocalDateTime;

public class UserResponseDto {

    public record UserResponse(Long id, String email, String name, LocalDateTime createdAt) {

        public static UserResponse from(User user) {
            return new UserResponse(user.getId(), user.getEmail(), user.getName(), user.getCreatedAt());
        }
    }

    public record LoginResponse(Long id, String email, String name, String tokenType) {

        public static LoginResponse from(User user) {
            // 인증 고도화 전까지는 tokenType만 응답한다.
            return new LoginResponse(user.getId(), user.getEmail(), user.getName(), "NONE");
        }
    }
}
