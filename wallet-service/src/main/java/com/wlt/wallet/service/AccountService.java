package com.wlt.wallet.service;

import com.wlt.wallet.dto.*;

public interface AccountService {
    GetWalletResponseDto getWallet(Long userId, Long id);
    WalletAccountResponseDto createWallet(Long userId, CreateWalletAccountRequestDto createWalletAccountRequestDto);
    AccountBalanceResponseDto creditAccountBalance(Long userId, CreditAccountBalanceRequestDto creditAccountBalanceRequestDto);
    AccountBalanceResponseDto debitAccountBalance(Long userId, DebitAccountBalanceRequestDto debitAccountBalanceRequestDto);

}
