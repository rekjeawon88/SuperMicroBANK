package com.bank.transaction.controller;

import com.bank.transaction.service.TransactionService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping("/count")
    public ResponseEntity<Map<String, Object>> getTransactionCount() {
        try {
            return ResponseEntity.ok(Map.of("count", transactionService.getTransactionCount()));
        } catch (Exception exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "거래 수 조회 중 오류가 발생했습니다."));
        }
    }
}
