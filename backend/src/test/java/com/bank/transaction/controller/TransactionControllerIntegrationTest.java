package com.bank.transaction.controller;

import com.bank.account.Account;
import com.bank.account.repository.AccountRepository;
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
@DisplayName("Transaction API 통합 테스트")
class TransactionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    private Long fromAccountId;
    private String fromAccountNumber;
    private Long toAccountId;
    private String toAccountNumber;

    @BeforeEach
    void setUp() {
        User user = userRepository.save(User.create("tx@bank.com", "pass1234", "거래유저"));

        Account from = accountRepository.save(Account.create(user, "111111111"));
        Account to = accountRepository.save(Account.create(user, "222222222"));

        fromAccountId = from.getId();
        fromAccountNumber = from.getAccountNumber();
        toAccountId = to.getId();
        toAccountNumber = to.getAccountNumber();
    }

    // =========================================================
    // POST /accounts/{accountId}/deposit (입금)
    // =========================================================
    @Nested
    @DisplayName("POST /accounts/{id}/deposit (입금)")
    class Deposit {

        @Test
        @DisplayName("정상 - 입금 시 200 반환 및 DEPOSIT 타입 확인")
        void deposit_success() throws Exception {
            Map<String, Long> body = Map.of("amount", 50000L);

            mockMvc.perform(post("/accounts/{accountId}/deposit", fromAccountId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.type").value("DEPOSIT"))
                    .andExpect(jsonPath("$.amount").value(50000));
        }

        @Test
        @DisplayName("예외 - 금액 0으로 입금 시 400 반환")
        void deposit_zeroAmount_returns400() throws Exception {
            Map<String, Long> body = Map.of("amount", 0L);

            mockMvc.perform(post("/accounts/{accountId}/deposit", fromAccountId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("예외 - 존재하지 않는 계좌에 입금 시 400 반환")
        void deposit_unknownAccount_returns400() throws Exception {
            Map<String, Long> body = Map.of("amount", 10000L);

            mockMvc.perform(post("/accounts/{accountId}/deposit", 99999L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("존재하지 않는 계좌입니다."));
        }
    }

    // =========================================================
    // POST /accounts/{accountId}/withdraw (출금)
    // =========================================================
    @Nested
    @DisplayName("POST /accounts/{id}/withdraw (출금)")
    class Withdraw {

        @BeforeEach
        void depositFirst() throws Exception {
            // 출금 테스트 전 잔액 충전
            Map<String, Long> body = Map.of("amount", 100000L);
            mockMvc.perform(post("/accounts/{accountId}/deposit", fromAccountId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(body)));
        }

        @Test
        @DisplayName("정상 - 잔액 이내 출금 시 200 반환 및 WITHDRAW 타입 확인")
        void withdraw_success() throws Exception {
            Map<String, Long> body = Map.of("amount", 30000L);

            mockMvc.perform(post("/accounts/{accountId}/withdraw", fromAccountId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.type").value("WITHDRAW"))
                    .andExpect(jsonPath("$.amount").value(30000));
        }

        @Test
        @DisplayName("예외 - 잔액 초과 출금 시 400 반환")
        void withdraw_insufficientBalance_returns400() throws Exception {
            Map<String, Long> body = Map.of("amount", 999999L);

            mockMvc.perform(post("/accounts/{accountId}/withdraw", fromAccountId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("잔액이 부족합니다."));
        }

        @Test
        @DisplayName("예외 - 금액 0으로 출금 시 400 반환")
        void withdraw_zeroAmount_returns400() throws Exception {
            Map<String, Long> body = Map.of("amount", 0L);

            mockMvc.perform(post("/accounts/{accountId}/withdraw", fromAccountId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isBadRequest());
        }
    }

    // =========================================================
    // POST /transfer (계좌 이체)
    // =========================================================
    @Nested
    @DisplayName("POST /transfer (계좌 이체)")
    class Transfer {

        @BeforeEach
        void depositFirst() throws Exception {
            Map<String, Long> body = Map.of("amount", 100000L);
            mockMvc.perform(post("/accounts/{accountId}/deposit", fromAccountId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(body)));
        }

        @Test
        @DisplayName("정상 - 계좌번호로 이체 시 200 반환 및 TRANSFER 타입 확인")
        void transfer_success() throws Exception {
            Map<String, Object> body = Map.of(
                    "fromAccountId", fromAccountId,
                    "toAccountNumber", toAccountNumber,
                    "amount", 40000L
            );

            mockMvc.perform(post("/transfer")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.type").value("TRANSFER"))
                    .andExpect(jsonPath("$.amount").value(40000))
                    .andExpect(jsonPath("$.fromAccountId").value(fromAccountId))
                    .andExpect(jsonPath("$.toAccountId").value(toAccountId));
        }

        @Test
        @DisplayName("예외 - 잔액 부족 이체 시 400 반환")
        void transfer_insufficientBalance_returns400() throws Exception {
            Map<String, Object> body = Map.of(
                    "fromAccountId", fromAccountId,
                    "toAccountNumber", toAccountNumber,
                    "amount", 999999L
            );

            mockMvc.perform(post("/transfer")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("잔액이 부족합니다."));
        }

        @Test
        @DisplayName("예외 - 동일 계좌 이체 시 400 반환")
        void transfer_sameAccount_returns400() throws Exception {
            Map<String, Object> body = Map.of(
                    "fromAccountId", fromAccountId,
                    "toAccountNumber", fromAccountNumber,
                    "amount", 10000L
            );

            mockMvc.perform(post("/transfer")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("동일 계좌로 이체할 수 없습니다."));
        }

        @Test
        @DisplayName("예외 - 존재하지 않는 계좌번호로 이체 시 400 반환")
        void transfer_unknownToAccount_returns400() throws Exception {
            Map<String, Object> body = Map.of(
                    "fromAccountId", fromAccountId,
                    "toAccountNumber", "000000000",
                    "amount", 10000L
            );

            mockMvc.perform(post("/transfer")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("입금 계좌번호를 찾을 수 없습니다."));
        }
    }

    // =========================================================
    // GET /accounts/{accountId}/transactions (거래 내역)
    // =========================================================
    @Nested
    @DisplayName("GET /accounts/{id}/transactions (거래 내역 조회)")
    class GetTransactions {

        @Test
        @DisplayName("정상 - 입금 후 거래 내역 조회 시 1건 반환")
        void getTransactions_afterDeposit() throws Exception {
            // 입금 1건
            mockMvc.perform(post("/accounts/{accountId}/deposit", fromAccountId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(Map.of("amount", 10000L))));

            mockMvc.perform(get("/accounts/{accountId}/transactions", fromAccountId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].type").value("DEPOSIT"));
        }

        @Test
        @DisplayName("정상 - 거래 없는 계좌 조회 시 빈 배열 반환")
        void getTransactions_empty() throws Exception {
            mockMvc.perform(get("/accounts/{accountId}/transactions", fromAccountId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }

        @Test
        @DisplayName("정상 - 입금·출금·이체 후 거래 내역 3건 반환 및 최신순 정렬 확인")
        void getTransactions_multipleTypes() throws Exception {
            // 입금
            mockMvc.perform(post("/accounts/{accountId}/deposit", fromAccountId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(Map.of("amount", 100000L))));

            // 출금
            mockMvc.perform(post("/accounts/{accountId}/withdraw", fromAccountId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(Map.of("amount", 20000L))));

            // 이체
            mockMvc.perform(post("/transfer")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(Map.of(
                            "fromAccountId", fromAccountId,
                            "toAccountNumber", toAccountNumber,
                            "amount", 10000L
                    ))));

            mockMvc.perform(get("/accounts/{accountId}/transactions", fromAccountId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(3));
        }
    }
}
