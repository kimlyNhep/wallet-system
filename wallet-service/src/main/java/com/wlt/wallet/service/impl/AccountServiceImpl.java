package com.wlt.wallet.service.impl;

import com.wlt.wallet.constants.CommonConstants;
import com.wlt.wallet.constants.MessageError;
import com.wlt.wallet.dto.*;
import com.wlt.wallet.entity.WalletAccount;
import com.wlt.wallet.exception.CustomException;
import com.wlt.wallet.provider.ServiceProvider;
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
    private final ServiceProvider serviceProvider;

    @Override
    public GetWalletResponseDto getWallet(Long userId, Long id) {
        Optional<WalletAccount> walletAccount = walletAccountRepository.findByIdAndStatus(id, CommonConstants.ACTIVE);

        if (walletAccount.isPresent()) {
            Long userWalletId = walletAccount.get().getUserId();
            if (!userId.equals(userWalletId)) {
                throw new CustomException(MessageError.ERR_004_CANNOT_QUERY_OTHER_WALLET);
            }
        }
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
        walletAccountResponseDto.setWalletId(newWalletAccount.getId());
        return walletAccountResponseDto;
    }

    @Override
    public AccountBalanceResponseDto creditAccountBalance(Long userId, CreditAccountBalanceRequestDto creditAccountBalanceRequestDto) {
        Optional<WalletAccount> creditWalletAccountOptional = walletAccountRepository.findByIdAndStatus(creditAccountBalanceRequestDto.getCreditWalletId(), CommonConstants.ACTIVE);
        if (creditWalletAccountOptional.isPresent()) {
            WalletAccount creditWalletAccount = creditWalletAccountOptional.get();
            String walletCcy = creditWalletAccount.getCcy();
            BigDecimal creditBalance = BigDecimal.ZERO;
            BigDecimal exchangeRate = BigDecimal.ZERO;
            if (!walletCcy.equals(creditAccountBalanceRequestDto.getCcy())) {
                GetExchangeRateRequestDto getExchangeRateRequestDto = new GetExchangeRateRequestDto();
                getExchangeRateRequestDto.setCrCcy(walletCcy);
                getExchangeRateRequestDto.setDrCcy(creditAccountBalanceRequestDto.getCcy());

                GetExchangeRateResponseDto exchangeRateResponseDto = serviceProvider.getExchangeRate(getExchangeRateRequestDto);
                if (exchangeRateResponseDto != null) {
                    exchangeRate = exchangeRateResponseDto.getRate();
                    creditBalance = creditAccountBalanceRequestDto.getCreditBalance().multiply(exchangeRate);
                    BigDecimal newBalance = creditWalletAccount.getBalance().add(creditBalance);
                    creditWalletAccount.setBalance(newBalance);
                    walletAccountRepository.save(creditWalletAccount);
                }
            } else {
                creditBalance = creditAccountBalanceRequestDto.getCreditBalance();
                BigDecimal newBalance = creditWalletAccount.getBalance().add(creditBalance);
                creditWalletAccount.setBalance(newBalance);
                walletAccountRepository.save(creditWalletAccount);
            }

            AccountBalanceResponseDto responseDto = new AccountBalanceResponseDto();
            responseDto.setStatus("SUCCESS");
            responseDto.setBalance(creditBalance);
            responseDto.setExchangeRate(exchangeRate.floatValue());
            responseDto.setCcy(walletCcy);
            return responseDto;

        } else {
            throw new CustomException(MessageError.ERR_002_WALLET_NOT_ENOUGH);
        }

    }

    @Override
    public AccountBalanceResponseDto debitAccountBalance(Long userId, DebitAccountBalanceRequestDto debitAccountBalanceRequestDto) {
        Optional<WalletAccount> debitWalletAccountOptional = walletAccountRepository.findByIdAndStatus(debitAccountBalanceRequestDto.getDebitWalletId(), CommonConstants.ACTIVE);
        if (debitWalletAccountOptional.isPresent()) {
            WalletAccount debitWalletAccount = debitWalletAccountOptional.get();
            String walletCcy = debitWalletAccount.getCcy();
            BigDecimal debitBalance = BigDecimal.ZERO;
            BigDecimal exchangeRate = BigDecimal.ZERO;

            if (!walletCcy.equals(debitAccountBalanceRequestDto.getCcy())) {
                GetExchangeRateRequestDto getExchangeRateRequestDto = new GetExchangeRateRequestDto();
                getExchangeRateRequestDto.setCrCcy(walletCcy);
                getExchangeRateRequestDto.setDrCcy(debitAccountBalanceRequestDto.getCcy());
                GetExchangeRateResponseDto exchangeRateResponseDto = serviceProvider.getExchangeRate(getExchangeRateRequestDto);

                if (exchangeRateResponseDto != null) {
                    exchangeRate = exchangeRateResponseDto.getRate();
                    BigDecimal balance = debitAccountBalanceRequestDto.getDebitBalance().multiply(exchangeRate);
                    if (debitWalletAccount.getBalance().compareTo(balance) > 0) {
                        debitBalance = balance;
                        BigDecimal newBalance = debitWalletAccount.getBalance().subtract(balance);
                        debitWalletAccount.setBalance(newBalance);
                        walletAccountRepository.save(debitWalletAccount);
                    } else {
                        throw new CustomException(MessageError.ERR_001_INSUFFICIENT_AMOUNT);
                    }
                }
            } else {
                debitBalance = debitAccountBalanceRequestDto.getDebitBalance();
                BigDecimal newBalance = debitWalletAccount.getBalance().subtract(debitBalance);
                debitWalletAccount.setBalance(newBalance);
                walletAccountRepository.save(debitWalletAccount);
            }

            AccountBalanceResponseDto responseDto = new AccountBalanceResponseDto();
            responseDto.setStatus("SUCCESS");
            responseDto.setBalance(debitBalance);
            responseDto.setExchangeRate(exchangeRate.floatValue());
            responseDto.setCcy(walletCcy);
            return responseDto;
        } else {
            throw new CustomException(MessageError.ERR_002_WALLET_NOT_ENOUGH);
        }
    }
}
