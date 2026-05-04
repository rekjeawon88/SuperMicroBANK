package com.bank.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("User API 통합 테스트")
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // =========================================================
    // POST /users (회원가입)
    // =========================================================
    @Nested
    @DisplayName("POST /users (회원가입)")
    class SignUp {

        @Test
        @DisplayName("정상 - 올바른 정보로 회원가입 시 201 반환 및 응답 필드 검증")
        void signUp_success() throws Exception {
            Map<String, String> body = Map.of(
                    "email", "test@bank.com",
                    "password", "password123",
                    "name", "홍길동"
            );

            mockMvc.perform(post("/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.email").value("test@bank.com"))
                    .andExpect(jsonPath("$.name").value("홍길동"))
                    .andExpect(jsonPath("$.id").isNumber());
        }

        @Test
        @DisplayName("예외 - 중복 이메일로 가입 시 400 반환")
        void signUp_duplicateEmail_returns400() throws Exception {
            Map<String, String> body = Map.of(
                    "email", "dup@bank.com",
                    "password", "password123",
                    "name", "중복유저"
            );

            // 첫 번째 가입
            mockMvc.perform(post("/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isCreated());

            // 두 번째 가입 (중복)
            mockMvc.perform(post("/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("이미 사용 중인 이메일입니다."));
        }

        @Test
        @DisplayName("예외 - 이메일 누락 시 400 반환")
        void signUp_missingEmail_returns400() throws Exception {
            Map<String, String> body = Map.of(
                    "password", "password123",
                    "name", "홍길동"
            );

            mockMvc.perform(post("/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isBadRequest());
        }
    }

    // =========================================================
    // POST /login (로그인)
    // =========================================================
    @Nested
    @DisplayName("POST /login (로그인)")
    class Login {

        @Test
        @DisplayName("정상 - 올바른 이메일·비밀번호로 로그인 시 200 및 사용자 정보 반환")
        void login_success() throws Exception {
            // 먼저 가입
            Map<String, String> signUpBody = Map.of(
                    "email", "login@bank.com",
                    "password", "pass1234",
                    "name", "로그인유저"
            );
            mockMvc.perform(post("/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(signUpBody)));

            // 로그인
            Map<String, String> loginBody = Map.of(
                    "email", "login@bank.com",
                    "password", "pass1234"
            );
            mockMvc.perform(post("/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginBody)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.email").value("login@bank.com"))
                    .andExpect(jsonPath("$.name").value("로그인유저"))
                    .andExpect(jsonPath("$.id").isNumber())
                    .andExpect(jsonPath("$.tokenType").value("NONE"));
        }

        @Test
        @DisplayName("예외 - 존재하지 않는 이메일로 로그인 시 401 반환")
        void login_unknownEmail_returns401() throws Exception {
            Map<String, String> body = Map.of(
                    "email", "nobody@bank.com",
                    "password", "pass1234"
            );

            mockMvc.perform(post("/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").value("사용자를 찾을 수 없습니다."));
        }

        @Test
        @DisplayName("예외 - 비밀번호 불일치 시 401 반환")
        void login_wrongPassword_returns401() throws Exception {
            // 가입
            Map<String, String> signUpBody = Map.of(
                    "email", "wrongpw@bank.com",
                    "password", "correct",
                    "name", "유저"
            );
            mockMvc.perform(post("/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(signUpBody)));

            // 틀린 비밀번호
            Map<String, String> loginBody = Map.of(
                    "email", "wrongpw@bank.com",
                    "password", "wrong"
            );
            mockMvc.perform(post("/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginBody)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").value("비밀번호가 올바르지 않습니다."));
        }
    }
}
