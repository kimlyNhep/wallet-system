package com.wlt.wallet.controller;

import com.wlt.wallet.dto.*;
import com.wlt.wallet.service.AccountService;
import com.wlt.wallet.service.GiftCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class WalletAccountController {
    private final AccountService accountService;
    private final GiftCodeService giftCodeService;

    @GetMapping("/api/v1/account/{id}")
    public ResponseEntity<SuccessResponse<GetWalletResponseDto>> getWalletResponse(
            @RequestHeader("user-id") Long userId,
            @PathVariable("id") Long id
    ) {
        SuccessResponse<GetWalletResponseDto> successResponse = new SuccessResponse<>();
        successResponse.setMessage("success");
        successResponse.setData(accountService.getWallet(userId, id));
        return ResponseEntity.ok(successResponse);
    }

    @PostMapping("/api/v1/gift-code/redeem")
    public ResponseEntity<SuccessResponse<RedeemGiftCodeResponseDto>> redeemGiftCode(
            @RequestHeader(name = "user-id") Long userId,
            @RequestBody RedeemGiftCodeRequestDto redeemGiftCodeRequestDto
    ) {
        SuccessResponse<RedeemGiftCodeResponseDto> successResponse = new SuccessResponse<>();
        successResponse.setData(giftCodeService.redeemGiftCode(userId, redeemGiftCodeRequestDto));
        successResponse.setMessage("success");
        return ResponseEntity.ok(successResponse);
    }

    @PostMapping("/api/v1/account/create")
    public ResponseEntity<SuccessResponse<WalletAccountResponseDto>> createWalletAccount(
            @RequestHeader(name = "user-id") Long userId,
            @RequestBody CreateWalletAccountRequestDto createWalletAccountRequestDto
    ) {
        SuccessResponse<WalletAccountResponseDto> successResponse = new SuccessResponse<>();
        successResponse.setData(accountService.createWallet(userId, createWalletAccountRequestDto));
        successResponse.setMessage("success");
        return ResponseEntity.ok(successResponse);
    }

    @PostMapping("/api/v1/account/credit")
    public ResponseEntity<SuccessResponse<AccountBalanceResponseDto>> creditWalletAccount(
            @RequestHeader(name = "user-id") Long userId,
            @RequestBody CreditAccountBalanceRequestDto creditAccountBalanceRequestDto
    ) {
        SuccessResponse<AccountBalanceResponseDto> successResponse = new SuccessResponse<>();
        successResponse.setData(accountService.creditAccountBalance(userId, creditAccountBalanceRequestDto));
        successResponse.setMessage("success");
        return ResponseEntity.ok(successResponse);
    }

    @PostMapping("/api/v1/account/debit")
    public ResponseEntity<SuccessResponse<AccountBalanceResponseDto>> debitWalletAccount(
            @RequestHeader(name = "user-id") Long userId,
            @RequestBody DebitAccountBalanceRequestDto debitAccountBalanceRequestDto
    ) {
        SuccessResponse<AccountBalanceResponseDto> successResponse = new SuccessResponse<>();
        successResponse.setData(accountService.debitAccountBalance(userId, debitAccountBalanceRequestDto));
        successResponse.setMessage("success");
        return ResponseEntity.ok(successResponse);
    }
}
