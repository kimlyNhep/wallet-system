package com.wlt.wallet.service.impl;

import com.wlt.wallet.constants.CommonConstants;
import com.wlt.wallet.dto.*;
import com.wlt.wallet.entity.WalletAccount;
import com.wlt.wallet.repository.WalletAccountRepository;
import com.wlt.wallet.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    @Value("${payment.service.base-url}")
    private String paymentServiceBaseUrl;

    private final WalletAccountRepository walletAccountRepository;
    private final RestTemplate restTemplate;

    @Override
    public GetWalletResponseDto getWallet(Long id) {
        Optional<WalletAccount> walletAccount = walletAccountRepository.findByIdAndStatus(id, CommonConstants.ACTIVE);
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

    @Override
    public AccountBalanceResponseDto creditAccountBalance(Long userId, CreditAccountBalanceRequestDto creditAccountBalanceRequestDto) {
        Optional<WalletAccount> creditWalletAccountOptional = walletAccountRepository.findByIdAndStatus(creditAccountBalanceRequestDto.getCreditWalletId(), CommonConstants.ACTIVE);
        if (creditWalletAccountOptional.isPresent()) {
            WalletAccount creditWalletAccount = creditWalletAccountOptional.get();
            String walletCcy = creditWalletAccount.getCcy();
            if (!walletCcy.equals(creditAccountBalanceRequestDto.getCcy())) {
                MultiValueMap<String, String> headers = new HttpHeaders();
                headers.add("Content-Type", "application/json");
                headers.add("Collation-id", UUID.randomUUID().toString());

                GetExchangeRateRequestDto getExchangeRateRequestDto = new GetExchangeRateRequestDto();
                getExchangeRateRequestDto.setCrCcy(walletCcy);
                getExchangeRateRequestDto.setDrCcy(creditAccountBalanceRequestDto.getCcy());

                ResponseEntity<SuccessResponse<GetExchangeRateResponseDto>> exchangeRateEntity = restTemplate.exchange(
                        paymentServiceBaseUrl + "/api/v1/exchange-rate",
                        HttpMethod.POST,
                        new HttpEntity<>(getExchangeRateRequestDto, headers),
                        new ParameterizedTypeReference<>() {
                        });

                if (exchangeRateEntity.getStatusCode().is2xxSuccessful()) {
                    SuccessResponse<GetExchangeRateResponseDto> rate = exchangeRateEntity.getBody();
                    if (rate != null && rate.getData() != null) {
                        GetExchangeRateResponseDto exchangeRateResponseDto = rate.getData();
                        BigDecimal balance = creditAccountBalanceRequestDto.getCreditBalance().multiply(exchangeRateResponseDto.getRate());
                        BigDecimal newBalance = creditWalletAccount.getBalance().add(balance);
                        creditWalletAccount.setBalance(newBalance);
                        walletAccountRepository.save(creditWalletAccount);
                    }
                }
            } else {
                BigDecimal balance = creditAccountBalanceRequestDto.getCreditBalance();
                BigDecimal newBalance = creditWalletAccount.getBalance().add(balance);
                creditWalletAccount.setBalance(newBalance);
                walletAccountRepository.save(creditWalletAccount);
            }
        } else {
            throw new RuntimeException("Wallet account not found");
        }

        AccountBalanceResponseDto responseDto = new AccountBalanceResponseDto();
        responseDto.setStatus("SUCCESS");
        return responseDto;
    }

    @Override
    public AccountBalanceResponseDto debitAccountBalance(Long userId, DebitAccountBalanceRequestDto debitAccountBalanceRequestDto) {
        Optional<WalletAccount> debitWalletAccountOptional = walletAccountRepository.findByIdAndStatus(debitAccountBalanceRequestDto.getDebitWalletId(), CommonConstants.ACTIVE);
        if (debitWalletAccountOptional.isPresent()) {
            WalletAccount debitWalletAccount = debitWalletAccountOptional.get();
            String walletCcy = debitWalletAccount.getCcy();
            if (!walletCcy.equals(debitAccountBalanceRequestDto.getCcy())) {
                MultiValueMap<String, String> headers = new HttpHeaders();
                headers.add("Content-Type", "application/json");
                headers.add("Collation-id", UUID.randomUUID().toString());

                GetExchangeRateRequestDto getExchangeRateRequestDto = new GetExchangeRateRequestDto();
                getExchangeRateRequestDto.setCrCcy(walletCcy);
                getExchangeRateRequestDto.setDrCcy(debitAccountBalanceRequestDto.getCcy());

                ResponseEntity<SuccessResponse<GetExchangeRateResponseDto>> exchangeRateEntity = restTemplate.exchange(
                        paymentServiceBaseUrl + "/api/v1/exchange-rate",
                        HttpMethod.POST,
                        new HttpEntity<>(getExchangeRateRequestDto, headers),
                        new ParameterizedTypeReference<>() {
                        });

                if (exchangeRateEntity.getStatusCode().is2xxSuccessful()) {
                    SuccessResponse<GetExchangeRateResponseDto> rate = exchangeRateEntity.getBody();
                    if (rate != null && rate.getData() != null) {
                        GetExchangeRateResponseDto exchangeRateResponseDto = rate.getData();
                        BigDecimal balance = debitAccountBalanceRequestDto.getDebitBalance().multiply(exchangeRateResponseDto.getRate());
                        if (debitWalletAccount.getBalance().compareTo(balance) > 0) {
                            BigDecimal newBalance = debitWalletAccount.getBalance().subtract(balance);
                            debitWalletAccount.setBalance(newBalance);
                            walletAccountRepository.save(debitWalletAccount);
                        } else {
                            throw new RuntimeException("insufficient balance");
                        }
                    }
                }
            } else {
                BigDecimal balance = debitAccountBalanceRequestDto.getDebitBalance();
                BigDecimal newBalance = debitWalletAccount.getBalance().subtract(balance);
                debitWalletAccount.setBalance(newBalance);
                walletAccountRepository.save(debitWalletAccount);
            }
        } else {
            throw new RuntimeException("Wallet account not found");
        }

        AccountBalanceResponseDto responseDto = new AccountBalanceResponseDto();
        responseDto.setStatus("SUCCESS");
        return responseDto;
    }
}
