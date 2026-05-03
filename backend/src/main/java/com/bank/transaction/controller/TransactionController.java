package com.bank.transaction.controller;

import com.bank.transaction.Transaction;
import com.bank.transaction.dto.TransactionRequestDto.AmountRequest;
import com.bank.transaction.dto.TransactionRequestDto.TransferRequest;
import com.bank.transaction.dto.TransactionResponseDto.TransactionDetailResponse;
import com.bank.transaction.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Transaction", description = "거래(입금·출금·이체) 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping
public class TransactionController {

    private final TransactionService transactionService;

    @Operation(summary = "입금", description = "특정 계좌에 금액을 입금합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "입금 성공",
                    content = @Content(schema = @Schema(implementation = TransactionDetailResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (유효하지 않은 계좌 또는 금액)"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PostMapping("/accounts/{accountId}/deposit")
    public ResponseEntity<?> deposit(
            @Parameter(description = "입금 대상 계좌 ID", example = "1")
            @PathVariable Long accountId,
            @RequestBody AmountRequest amountRequest) {
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

    @Operation(summary = "출금", description = "특정 계좌에서 금액을 출금합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "출금 성공",
                    content = @Content(schema = @Schema(implementation = TransactionDetailResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (잔액 부족 또는 유효하지 않은 계좌)"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PostMapping("/accounts/{accountId}/withdraw")
    public ResponseEntity<?> withdraw(
            @Parameter(description = "출금 대상 계좌 ID", example = "1")
            @PathVariable Long accountId,
            @RequestBody AmountRequest amountRequest) {
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

    @Operation(summary = "계좌 이체", description = "출금 계좌 ID에서 입금 계좌번호로 금액을 이체합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "이체 성공",
                    content = @Content(schema = @Schema(implementation = TransactionDetailResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (잔액 부족, 동일 계좌 이체 등)"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PostMapping("/transfer")
    public ResponseEntity<?> transfer(@RequestBody TransferRequest transferRequest) {
        try {
            Transaction transferTransaction = transactionService.transfer(
                    transferRequest.fromAccountId(),
                    transferRequest.toAccountNumber(),
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

    @Operation(summary = "거래 내역 조회", description = "특정 계좌의 모든 거래 내역을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = TransactionDetailResponse.class)))),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @GetMapping("/accounts/{accountId}/transactions")
    public ResponseEntity<?> getTransactions(
            @Parameter(description = "조회할 계좌 ID", example = "1")
            @PathVariable Long accountId) {
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
