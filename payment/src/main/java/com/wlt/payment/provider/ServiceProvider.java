package com.wlt.payment.provider;

import com.wlt.payment.config.ApplicationPropertyConfig;
import com.wlt.payment.constants.MessageError;
import com.wlt.payment.dto.*;
import com.wlt.payment.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ServiceProvider extends ApplicationPropertyConfig {

    private final RestTemplate restTemplate;

    public WalletAccountResponseDto getWalletAccount(Long userId, Long walletId) {
       MultiValueMap<String, String> headers = getHeaders();
        headers.add("user-id", String.valueOf(userId));

        ResponseEntity<SuccessResponse<WalletAccountResponseDto>> crWalletResponseEntity = restTemplate.exchange(
                walletServiceUrl + "/api/v1/account/" + walletId,
                HttpMethod.GET,
                new HttpEntity<>(null, headers),
                new ParameterizedTypeReference<>() {
                }
        );

        if (crWalletResponseEntity.getStatusCode().is2xxSuccessful()) {
            SuccessResponse<WalletAccountResponseDto> response = crWalletResponseEntity.getBody();
            if (response != null && response.getData() != null) {
                return response.getData();
            }
        }

        throw new CustomException(MessageError.ERR_002_WALLET_NOT_ENOUGH);
    }

    public AccountBalanceResponseDto creditAccountBalance(Long userId, CreditAccountBalanceRequestDto creditAccountBalanceRequestDto) {
        MultiValueMap<String, String> headers = getHeaders();
        headers.add("user-id", String.valueOf(userId));

        ResponseEntity<SuccessResponse<AccountBalanceResponseDto>> creditBalanceResponseEntity = restTemplate.exchange(
                walletServiceUrl + "/api/v1/account/credit",
                HttpMethod.POST,
                new HttpEntity<>(creditAccountBalanceRequestDto, headers),
                new ParameterizedTypeReference<>() {
                }
        );

        if (creditBalanceResponseEntity.getStatusCode().is2xxSuccessful()) {
            SuccessResponse<AccountBalanceResponseDto> response = creditBalanceResponseEntity.getBody();
            if (response != null && response.getData() != null) {
                return response.getData();
            }
        }

        throw new CustomException(MessageError.ERR_006_ERROR_CREDIT_WALLET);
    }

    public AccountBalanceResponseDto debitAccountBalance(Long userId, DebitAccountBalanceRequestDto detailsRequestDto) {
        MultiValueMap<String, String> headers = getHeaders();
        headers.add("user-id", String.valueOf(userId));

        ResponseEntity<SuccessResponse<AccountBalanceResponseDto>> debitBalanceResponseEntity = restTemplate.exchange(
                walletServiceUrl + "/api/v1/account/debit",
                HttpMethod.POST,
                new HttpEntity<>(detailsRequestDto, headers),
                new ParameterizedTypeReference<>() {
                }
        );

        if (debitBalanceResponseEntity.getStatusCode().is2xxSuccessful()) {
            SuccessResponse<AccountBalanceResponseDto> response = debitBalanceResponseEntity.getBody();
            if (response != null && response.getData() != null) {
                return response.getData();
            }
        }

        throw new CustomException(MessageError.ERR_006_ERROR_CREDIT_WALLET);
    }

    private MultiValueMap<String, String> getHeaders() {
        MultiValueMap<String, String> headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        headers.add("Collation-id", UUID.randomUUID().toString());
        return headers;
    }
}
