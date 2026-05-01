package com.bank.user.dto;

public class UserRequestDto {

    public record SignUpRequest(String email, String password, String name) {
    }

    public record LoginRequest(String email, String password) {
    }
}
