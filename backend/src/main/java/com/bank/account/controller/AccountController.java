package com.bank.account.controller;

import com.bank.account.Account;
import com.bank.account.dto.AccountRequestDto.CreateAccountRequest;
import com.bank.account.dto.AccountResponseDto.AccountDetailResponse;
import com.bank.account.service.AccountService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping
public class AccountController {

    private final AccountService accountService;

    @PostMapping("/accounts")
    public ResponseEntity<?> createAccount(@RequestBody CreateAccountRequest createAccountRequest) {
        try {
            Account createdAccount = accountService.createAccount(createAccountRequest.userId());
            return ResponseEntity.status(HttpStatus.CREATED).body(AccountDetailResponse.from(createdAccount));
        } catch (IllegalArgumentException illegalArgumentException) {
            return ResponseEntity.badRequest().body(Map.of("message", illegalArgumentException.getMessage()));
        } catch (Exception exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "계좌 생성 중 오류가 발생했습니다."));
        }
    }

    @GetMapping("/accounts/{accountId}")
    public ResponseEntity<?> getAccount(@PathVariable Long accountId) {
        try {
            Account foundAccount = accountService.getAccountById(accountId);
            return ResponseEntity.ok(AccountDetailResponse.from(foundAccount));
        } catch (IllegalArgumentException illegalArgumentException) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", illegalArgumentException.getMessage()));
        } catch (Exception exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "계좌 상세 조회 중 오류가 발생했습니다."));
        }
    }

    @GetMapping("/users/{userId}/accounts")
    public ResponseEntity<?> getUserAccounts(@PathVariable Long userId) {
        try {
            List<AccountDetailResponse> accountDetailResponseList = accountService.getAccountsByUserId(userId).stream()
                    .map(AccountDetailResponse::from)
                    .toList();
            return ResponseEntity.ok(accountDetailResponseList);
        } catch (Exception exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "계좌 목록 조회 중 오류가 발생했습니다."));
        }
    }
}
