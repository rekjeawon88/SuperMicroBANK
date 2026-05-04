package com.bank.account.controller;

import com.bank.user.User;
import com.bank.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Account API 통합 테스트")
class AccountControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    private Long savedUserId;

    @BeforeEach
    void setUp() {
        User user = User.create("account@bank.com", "pass1234", "계좌유저");
        savedUserId = userRepository.save(user).getId();
    }

    // =========================================================
    // POST /accounts (계좌 생성)
    // =========================================================
    @Nested
    @DisplayName("POST /accounts (계좌 생성)")
    class CreateAccount {

        @Test
        @DisplayName("정상 - 유효한 userId로 계좌 생성 시 201 반환 및 초기 잔액 0 확인")
        void createAccount_success() throws Exception {
            Map<String, Long> body = Map.of("userId", savedUserId);

            mockMvc.perform(post("/accounts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").isNumber())
                    .andExpect(jsonPath("$.userId").value(savedUserId))
                    .andExpect(jsonPath("$.accountNumber").isString())
                    .andExpect(jsonPath("$.balance").value(0));
        }

        @Test
        @DisplayName("예외 - 존재하지 않는 userId로 계좌 생성 시 400 반환")
        void createAccount_invalidUser_returns400() throws Exception {
            Map<String, Long> body = Map.of("userId", 99999L);

            mockMvc.perform(post("/accounts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("존재하지 않는 사용자입니다."));
        }
    }

    // =========================================================
    // GET /accounts/{accountId} (계좌 단건 조회)
    // =========================================================
    @Nested
    @DisplayName("GET /accounts/{accountId} (계좌 단건 조회)")
    class GetAccount {

        @Test
        @DisplayName("정상 - 존재하는 계좌 ID로 조회 시 200 반환")
        void getAccount_success() throws Exception {
            // 계좌 생성 후 ID 추출
            Map<String, Long> body = Map.of("userId", savedUserId);
            String response = mockMvc.perform(post("/accounts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body)))
                    .andReturn().getResponse().getContentAsString();

            Long accountId = objectMapper.readTree(response).get("id").asLong();

            mockMvc.perform(get("/accounts/{accountId}", accountId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(accountId))
                    .andExpect(jsonPath("$.balance").value(0));
        }

        @Test
        @DisplayName("예외 - 존재하지 않는 계좌 ID 조회 시 404 반환")
        void getAccount_notFound_returns404() throws Exception {
            mockMvc.perform(get("/accounts/{accountId}", 99999L))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("존재하지 않는 계좌입니다."));
        }
    }

    // =========================================================
    // GET /users/{userId}/accounts (사용자 계좌 목록)
    // =========================================================
    @Nested
    @DisplayName("GET /users/{userId}/accounts (사용자 계좌 목록)")
    class GetUserAccounts {

        @Test
        @DisplayName("정상 - 계좌 2개 생성 후 목록 조회 시 2건 반환")
        void getUserAccounts_returnsList() throws Exception {
            Map<String, Long> body = Map.of("userId", savedUserId);

            // 계좌 2개 생성
            mockMvc.perform(post("/accounts")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(body)));
            mockMvc.perform(post("/accounts")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(body)));

            mockMvc.perform(get("/users/{userId}/accounts", savedUserId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2));
        }

        @Test
        @DisplayName("정상 - 계좌가 없는 사용자는 빈 배열 반환")
        void getUserAccounts_empty() throws Exception {
            mockMvc.perform(get("/users/{userId}/accounts", savedUserId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }
    }
}
