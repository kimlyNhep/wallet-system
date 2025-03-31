package com.wlt.wallet.provider;

import com.wlt.wallet.config.ApplicationPropertyConfig;
import com.wlt.wallet.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ServiceProvider extends ApplicationPropertyConfig {

    private final RestTemplate restTemplate;

    public GetExchangeRateResponseDto getExchangeRate(GetExchangeRateRequestDto requestDto) {
        ResponseEntity<SuccessResponse<GetExchangeRateResponseDto>> exchangeRateEntity = restTemplate.exchange(
                paymentServiceBaseUrl + "/api/v1/exchange-rate",
                HttpMethod.POST,
                new HttpEntity<>(requestDto, getHeaders()),
                new ParameterizedTypeReference<>() {
                });

        if (exchangeRateEntity.getStatusCode().is2xxSuccessful()) {
            SuccessResponse<GetExchangeRateResponseDto> rate = exchangeRateEntity.getBody();
            if (rate != null && rate.getData() != null) {
                return rate.getData();
            }
        }

        throw new RuntimeException("Could not get exchange rate");
    }

    public RedeemCodeResponseDto redeemGiftCode(Long userId, GiftCodeRedeemEvent requestDto) {
        MultiValueMap<String, String> headers = getHeaders();
        headers.add("user-id", String.valueOf(userId));

        ResponseEntity<SuccessResponse<RedeemCodeResponseDto>> redeemResponseEntity = restTemplate.exchange(
                paymentServiceBaseUrl + "/api/v1/gift-code/redeem",
                HttpMethod.POST,
                new HttpEntity<>(requestDto, headers),
                new ParameterizedTypeReference<>() {
                });

        if (redeemResponseEntity.getStatusCode().is2xxSuccessful()) {
            SuccessResponse<RedeemCodeResponseDto> redeemCodeResponse = redeemResponseEntity.getBody();
            if (redeemCodeResponse != null && redeemCodeResponse.getData() != null) {
                return redeemCodeResponse.getData();
            }
        }

        throw new RuntimeException("Could not redeem gift code");
    }

    private MultiValueMap<String, String> getHeaders() {
        MultiValueMap<String, String> headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        headers.add("Collation-id", UUID.randomUUID().toString());
        return headers;
    }
}
