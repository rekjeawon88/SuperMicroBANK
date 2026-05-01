package com.bank.transaction.controller;

import com.bank.transaction.Transaction;
import com.bank.transaction.dto.TransactionRequestDto.AmountRequest;
import com.bank.transaction.dto.TransactionRequestDto.TransferRequest;
import com.bank.transaction.dto.TransactionResponseDto.TransactionDetailResponse;
import com.bank.transaction.service.TransactionService;
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
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/accounts/{accountId}/deposit")
    public ResponseEntity<?> deposit(@PathVariable Long accountId, @RequestBody AmountRequest amountRequest) {
        try {
            Transaction depositTransaction = transactionService.deposit(accountId, amountRequest.amount());
            return ResponseEntity.ok(TransactionDetailResponse.from(depositTransaction));
        } catch (IllegalArgumentException illegalArgumentException) {
            return ResponseEntity.badRequest().body(Map.of("message", illegalArgumentException.getMessage()));
        } catch (Exception exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "입금 처리 중 오류가 발생했습니다."));
        }
    }

    @PostMapping("/accounts/{accountId}/withdraw")
    public ResponseEntity<?> withdraw(@PathVariable Long accountId, @RequestBody AmountRequest amountRequest) {
        try {
            Transaction withdrawTransaction = transactionService.withdraw(accountId, amountRequest.amount());
            return ResponseEntity.ok(TransactionDetailResponse.from(withdrawTransaction));
        } catch (IllegalArgumentException illegalArgumentException) {
            return ResponseEntity.badRequest().body(Map.of("message", illegalArgumentException.getMessage()));
        } catch (Exception exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "출금 처리 중 오류가 발생했습니다."));
        }
    }

    @PostMapping("/transfer")
    public ResponseEntity<?> transfer(@RequestBody TransferRequest transferRequest) {
        try {
            Transaction transferTransaction = transactionService.transfer(
                    transferRequest.fromAccountId(),
                    transferRequest.toAccountId(),
                    transferRequest.amount()
            );
            return ResponseEntity.ok(TransactionDetailResponse.from(transferTransaction));
        } catch (IllegalArgumentException illegalArgumentException) {
            return ResponseEntity.badRequest().body(Map.of("message", illegalArgumentException.getMessage()));
        } catch (Exception exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "이체 처리 중 오류가 발생했습니다."));
        }
    }

    @GetMapping("/accounts/{accountId}/transactions")
    public ResponseEntity<?> getTransactions(@PathVariable Long accountId) {
        try {
            List<TransactionDetailResponse> transactionDetailResponseList =
                    transactionService.getTransactionsByAccountId(accountId).stream()
                            .map(TransactionDetailResponse::from)
                            .toList();
            return ResponseEntity.ok(transactionDetailResponseList);
        } catch (Exception exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "거래내역 조회 중 오류가 발생했습니다."));
        }
    }
}
