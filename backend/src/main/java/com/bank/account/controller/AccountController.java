package com.bank.account.controller;

import com.bank.account.service.AccountService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    @GetMapping("/count")
    public ResponseEntity<Map<String, Object>> getAccountCount() {
        try {
            return ResponseEntity.ok(Map.of("count", accountService.getAccountCount()));
        } catch (Exception exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "계좌 수 조회 중 오류가 발생했습니다."));
        }
    }
}
