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

import java.util.Optional;
import java.util.UUID;

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
        Optional<WalletAccount> walletAccountOptional = walletAccountRepository.findByIdAndStatus(requestDto.getCreditWalletId(), CommonConstants.ACTIVE);
        if (walletAccountOptional.isPresent()) {
            GiftCodeRedeemEvent giftCodeRedeemEvent = new GiftCodeRedeemEvent();
            giftCodeRedeemEvent.setUserId(userId);
            giftCodeRedeemEvent.setGiftCode(requestDto.getGiftCode());
            giftCodeRedeemEvent.setCreditWalletId(requestDto.getCreditWalletId());

            MultiValueMap<String, String> headers = new HttpHeaders();
            headers.add("Content-Type", "application/json");
            headers.add("Collation-id", UUID.randomUUID().toString());
            headers.add("user-id", String.valueOf(userId));

            ResponseEntity<SuccessResponse<RedeemCodeResponseDto>> redeemResponseEntity = restTemplate.exchange(
                    paymentServiceBaseUrl + "/api/v1/gift-code/redeem",
                    HttpMethod.POST,
                    new HttpEntity<>(giftCodeRedeemEvent, headers),
                    new ParameterizedTypeReference<>() {
                    });

            if (redeemResponseEntity.getStatusCode().is2xxSuccessful()) {
                SuccessResponse<RedeemCodeResponseDto> redeemCodeResponse = redeemResponseEntity.getBody();
                if (redeemCodeResponse != null && redeemCodeResponse.getData() != null) {
                    String redeemStatus = redeemCodeResponse.getData().getStatus();
                    if ("REDEEMED".equals(redeemStatus)) {
                        RedeemGiftCodeResponseDto response = new RedeemGiftCodeResponseDto();
                        response.setGiftCode(requestDto.getGiftCode());
                        response.setDescription("Success redeem code");
                        return response;
                    }
                }
            }
        }

        throw new RuntimeException("Cannot redeem gift code");
    }
}
