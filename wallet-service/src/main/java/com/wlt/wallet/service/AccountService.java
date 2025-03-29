package com.wlt.wallet.service;

import com.wlt.wallet.dto.*;

import java.math.BigDecimal;

public interface AccountService {
    GetWalletResponseDto getWallet(Long userId);
    WalletAccountResponseDto createWallet(Long userId, CreateWalletAccountRequestDto createWalletAccountRequestDto);
}
