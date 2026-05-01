package com.bank.user.controller;

import com.bank.user.User;
import com.bank.user.dto.UserRequestDto.LoginRequest;
import com.bank.user.dto.UserRequestDto.SignUpRequest;
import com.bank.user.dto.UserResponseDto.LoginResponse;
import com.bank.user.dto.UserResponseDto.UserResponse;
import com.bank.user.service.UserService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping
public class UserController {

    private final UserService userService;

    @PostMapping("/users")
    public ResponseEntity<?> signUp(@RequestBody SignUpRequest signUpRequest) {
        try {
            User signedUpUser = userService.signUp(
                    signUpRequest.email(),
                    signUpRequest.password(),
                    signUpRequest.name()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.from(signedUpUser));
        } catch (IllegalArgumentException illegalArgumentException) {
            return ResponseEntity.badRequest().body(Map.of("message", illegalArgumentException.getMessage()));
        } catch (Exception exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "회원가입 중 오류가 발생했습니다."));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            User loggedInUser = userService.login(loginRequest.email(), loginRequest.password());
            return ResponseEntity.ok(LoginResponse.from(loggedInUser));
        } catch (IllegalArgumentException illegalArgumentException) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", illegalArgumentException.getMessage()));
        } catch (Exception exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "로그인 중 오류가 발생했습니다."));
        }
    }
}
