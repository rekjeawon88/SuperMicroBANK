package com.bank.account.controller;

import com.bank.account.Account;
import com.bank.account.dto.AccountRequestDto.CreateAccountRequest;
import com.bank.account.dto.AccountResponseDto.AccountDetailResponse;
import com.bank.account.service.AccountService;
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

@Tag(name = "Account", description = "계좌 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping
public class AccountController {

    private final AccountService accountService;

    @Operation(summary = "계좌 생성", description = "사용자 ID를 기반으로 새 계좌를 생성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "계좌 생성 성공",
                    content = @Content(schema = @Schema(implementation = AccountDetailResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (존재하지 않는 사용자 등)"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
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

    @Operation(summary = "계좌 단건 조회", description = "계좌 ID로 계좌 상세 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = AccountDetailResponse.class))),
            @ApiResponse(responseCode = "404", description = "계좌를 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @GetMapping("/accounts/{accountId}")
    public ResponseEntity<?> getAccount(
            @Parameter(description = "조회할 계좌 ID", example = "1")
            @PathVariable Long accountId) {
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

    @Operation(summary = "사용자 계좌 목록 조회", description = "특정 사용자의 모든 계좌 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = AccountDetailResponse.class)))),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @GetMapping("/users/{userId}/accounts")
    public ResponseEntity<?> getUserAccounts(
            @Parameter(description = "조회할 사용자 ID", example = "1")
            @PathVariable Long userId) {
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