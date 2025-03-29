package com.wlt.wallet.service.impl;

import com.wlt.wallet.constants.CommonConstants;
import com.wlt.wallet.dto.*;
import com.wlt.wallet.entity.WalletAccount;
import com.wlt.wallet.repository.WalletAccountRepository;
import com.wlt.wallet.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final WalletAccountRepository walletAccountRepository;

    @Override
    public GetWalletResponseDto getWallet(Long userId) {
        Optional<WalletAccount> walletAccount = walletAccountRepository.findByUserId(userId);
        GetWalletResponseDto getWalletResponseDto = new GetWalletResponseDto();
        walletAccount.ifPresent(account -> BeanUtils.copyProperties(account, getWalletResponseDto));
        return getWalletResponseDto;
    }

    @Override
    public WalletAccountResponseDto createWallet(Long userId, CreateWalletAccountRequestDto createWalletAccountRequestDto) {
        WalletAccount walletAccount = new WalletAccount();
        walletAccount.setBalance(BigDecimal.ZERO);
        walletAccount.setUserId(userId);
        walletAccount.setCcy(createWalletAccountRequestDto.getCcy());
        walletAccount.setStatus(CommonConstants.ACTIVE);
        walletAccount.setBlockBalance(BigDecimal.ZERO);
        WalletAccount newWalletAccount = walletAccountRepository.save(walletAccount);
        WalletAccountResponseDto walletAccountResponseDto = new WalletAccountResponseDto();
        BeanUtils.copyProperties(newWalletAccount, walletAccountResponseDto);
        return walletAccountResponseDto;
    }
}
