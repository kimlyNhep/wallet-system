package com.wlt.wallet.service.impl;

import com.wlt.wallet.constants.CommonConstants;
import com.wlt.wallet.dto.*;
import com.wlt.wallet.entity.WalletAccount;
import com.wlt.wallet.repository.WalletAccountRepository;
import com.wlt.wallet.service.GiftCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
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
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class GiftCodeServiceImpl implements GiftCodeService {

    @Value("${payment.service.base-url}")
    private String paymentServiceBaseUrl;

    @Value("${rabbitmq.exchange.gift-code}")
    private String giftCodeExchange;

    @Value("${rabbitmq.routing-key.gift-code.redeem}")
    private String redeemGiftCodeRoutingKey;

    private final RestTemplate restTemplate;
    private final RabbitTemplate rabbitTemplate;
    private final WalletAccountRepository walletAccountRepository;

    @Override
    public RedeemGiftCodeResponseDto redeemGiftCode(Long userId, RedeemGiftCodeRequestDto requestDto) {
        try {
            MultiValueMap<String, String> headers = new HttpHeaders();
            headers.add("Content-Type", "application/json");
            headers.add("Collation-id", UUID.randomUUID().toString());

            ResponseEntity<SuccessResponse<GiftCodeValidationResponseDto>> giftCodeValidationResponse = restTemplate.exchange(
                    paymentServiceBaseUrl + "/api/v1/validate/gift-code/" + requestDto.getGiftCode(),
                    HttpMethod.GET,
                    new HttpEntity<>(null, headers),
                    new ParameterizedTypeReference<>() {
                    }
            );

            if (giftCodeValidationResponse.getStatusCode().is2xxSuccessful()) {
                SuccessResponse<GiftCodeValidationResponseDto> responseBody = giftCodeValidationResponse.getBody();
                if (responseBody != null && responseBody.getData() != null) {

                    if (!responseBody.getData().isValid()) {
                        RedeemGiftCodeResponseDto response = new RedeemGiftCodeResponseDto();
                        response.setDescription("Gift code validation failed");
                        return response;
                    }

                    BigDecimal giftAmount = responseBody.getData().getAmount();
                    String ccy = responseBody.getData().getCcy();

                    Optional<WalletAccount> walletAccountOptional = walletAccountRepository.findByIdAndStatus(requestDto.getCreditWalletId(), CommonConstants.ACTIVE);
                    if (walletAccountOptional.isPresent()) {
                        WalletAccount walletAccount = walletAccountOptional.get();
                        if (giftAmount.compareTo(BigDecimal.ZERO) > 0) {
                            if (!walletAccount.getCcy().equals(ccy)) {
                                GetExchangeRateRequestDto getExchangeRateRequestDto = new GetExchangeRateRequestDto();
                                getExchangeRateRequestDto.setCrCcy(walletAccount.getCcy());
                                getExchangeRateRequestDto.setDrCcy(ccy);

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
                                        BigDecimal balance = giftAmount.multiply(exchangeRateResponseDto.getRate());
                                        BigDecimal newBalance = walletAccount.getBalance().add(balance);
                                        walletAccount.setBalance(newBalance);
                                        walletAccountRepository.save(walletAccount);
                                    }
                                }
                            }
                            else {
                                BigDecimal balance = walletAccount.getBalance().add(giftAmount);
                                walletAccount.setBalance(balance);
                                walletAccountRepository.save(walletAccount);
                            }
                            // record transaction

                            // update gift code to redeem
                            CompletableFuture.runAsync(() -> {
                                GiftCodeRedeemEvent giftCodeRedeemEvent = new GiftCodeRedeemEvent();
                                giftCodeRedeemEvent.setUserId(userId);
                                giftCodeRedeemEvent.setGiftCode(requestDto.getGiftCode());
                                rabbitTemplate.convertAndSend(giftCodeExchange, redeemGiftCodeRoutingKey, giftCodeRedeemEvent);
                            });

                            RedeemGiftCodeResponseDto response = new RedeemGiftCodeResponseDto();
                            response.setDescription("Gift code redeemed successfully. Wallet credited with" + giftAmount + " \"" + ccy);
                            return response;
                        } else {
                            RedeemGiftCodeResponseDto response = new RedeemGiftCodeResponseDto();
                            response.setDescription("Couldn't redeem gift code");
                            return response;
                        }
                    } else {
                        RedeemGiftCodeResponseDto response = new RedeemGiftCodeResponseDto();
                        response.setDescription("Wallet account not found");
                    }
                }
            } else {
                RedeemGiftCodeResponseDto response = new RedeemGiftCodeResponseDto();
                response.setDescription("Invalid gift code.");
                return response;
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to get redeem gift code", e);
        }

        RedeemGiftCodeResponseDto response = new RedeemGiftCodeResponseDto();
        response.setDescription("Failed to get redeem gift code");
        return response;
    }
}
