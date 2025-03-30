package com.wlt.payment.service.impl;

import com.wlt.payment.config.ApplicationPropertyConfig;
import com.wlt.payment.dto.*;
import com.wlt.payment.entity.GiftCode;
import com.wlt.payment.repository.GiftCodeRepository;
import com.wlt.payment.service.PaymentService;
import com.wlt.payment.service.TopUpBalanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class TopUpBalanceServiceImpl extends ApplicationPropertyConfig implements TopUpBalanceService {

    @Value("${wallet.service.base-url}")
    private String walletServiceUrl;
    private final RestTemplate restTemplate;
    private final RabbitTemplate rabbitTemplate;

    private final GiftCodeRepository giftCodeRepository;

    private static final String CODE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 12;

    @Override
    public GiftCodeValidationResponseDto validateGiftCode(String code) {
        Optional<GiftCode> giftCodeOptional = giftCodeRepository.findByGiftCode(code);

        if (giftCodeOptional.isPresent()) {
            GiftCode giftCode = giftCodeOptional.get();
            if (giftCode.getIsRedeemed().equals("N") && (giftCode.getExpiryDateTime() == null || giftCode.getExpiryDateTime().isAfter(LocalDateTime.now()))) {
                GiftCodeValidationResponseDto responseDto = new GiftCodeValidationResponseDto();
                responseDto.setGiftCode(code);
                responseDto.setAmount(giftCode.getAmount());
                responseDto.setCcy(giftCode.getCcy());
                responseDto.setValid(true);
                return responseDto;
            } else {
                GiftCodeValidationResponseDto responseDto = new GiftCodeValidationResponseDto();
                responseDto.setGiftCode(code);
                responseDto.setAmount(giftCode.getAmount());
                responseDto.setCcy(giftCode.getCcy());
                responseDto.setValid(false);
                return responseDto;
            }
        }

        throw new RuntimeException("GiftCode not found");
    }

    @Override
    public void markGiftCodeAsRedeemed(Long userId, String code) {
        Optional<GiftCode> giftCodeOptional = giftCodeRepository.findByGiftCode(code);
        giftCodeOptional.ifPresent(giftCode -> {
            giftCode.setIsRedeemed("Y");
            giftCode.setRedeemedBy(userId);
            giftCodeRepository.save(giftCode);
        });
    }

    @Override
    public GenerateGiftCodeResponseDto generateGiftCode(GenerateGiftCodeRequestDto generateGiftCodeRequestDto) {
        if (generateGiftCodeRequestDto.getAmount() == null || generateGiftCodeRequestDto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Gift code value must be positive.");
        }
        if (generateGiftCodeRequestDto.getCcy() == null || generateGiftCodeRequestDto.getCcy().trim().isEmpty()) {
            throw new IllegalArgumentException("Gift code currency must be specified.");
        }

        String code = generateUniqueCode();
        GiftCode giftCode = new GiftCode();
        giftCode.setGiftCode(code);
        giftCode.setAmount(generateGiftCodeRequestDto.getAmount());
        giftCode.setCcy(generateGiftCodeRequestDto.getCcy());
        giftCode.setExpiryDateTime(generateGiftCodeRequestDto.getExpireDate());
        giftCode.setIsRedeemed("N");
        giftCodeRepository.save(giftCode);
        log.info("Generated gift code: {} with value {} {}", code, generateGiftCodeRequestDto.getAmount(), generateGiftCodeRequestDto.getCcy());
        GenerateGiftCodeResponseDto responseDto = new GenerateGiftCodeResponseDto();
        responseDto.setGiftCode(code);
        return responseDto;
    }

    @Override
    public RedeemGiftCodeResponseDto redeemGiftCode(Long userId, GiftCodeRedeemEvent giftCodeRedeemEvent) {
        GiftCodeValidationResponseDto giftCodeValidationResponseDto = validateGiftCode(giftCodeRedeemEvent.getGiftCode());
        if (giftCodeValidationResponseDto.isValid()) {
            BigDecimal giftAmount = giftCodeValidationResponseDto.getAmount();
            String giftCodeCcy = giftCodeValidationResponseDto.getCcy();

            MultiValueMap<String, String> headers = new HttpHeaders();
            headers.add("Content-Type", "application/json");
            headers.add("Collation-id", UUID.randomUUID().toString());
            headers.add("user-id", String.valueOf(userId));

            ResponseEntity<SuccessResponse<WalletAccountResponseDto>> crWalletResponseEntity = restTemplate.exchange(
                    walletServiceUrl + "/api/v1/account/" + giftCodeRedeemEvent.getCreditWalletId(),
                    HttpMethod.GET,
                    new HttpEntity<>(null, headers),
                    new ParameterizedTypeReference<>() {
                    }
            );

            if (crWalletResponseEntity.getStatusCode().is2xxSuccessful()) {
                SuccessResponse<WalletAccountResponseDto> crWalletResponseEntityBody = crWalletResponseEntity.getBody();
                if (crWalletResponseEntityBody != null && crWalletResponseEntityBody.getData() != null) {
                    GetExchangeRateAmountRequestDto exchangeRateAmountRequestDto = new GetExchangeRateAmountRequestDto();
                    exchangeRateAmountRequestDto.setCrCcy(crWalletResponseEntityBody.getData().getCcy());
                    exchangeRateAmountRequestDto.setDrCcy(giftCodeCcy);
                    CreditAccountBalanceRequestDto creditAccountBalanceRequestDto = new CreditAccountBalanceRequestDto();
                    creditAccountBalanceRequestDto.setCcy(giftCodeCcy);
                    creditAccountBalanceRequestDto.setCreditBalance(giftAmount);
                    creditAccountBalanceRequestDto.setCreditWalletId(giftCodeRedeemEvent.getCreditWalletId());

                    markGiftCodeAsRedeemed(userId, giftCodeRedeemEvent.getGiftCode());

                    CompletableFuture.runAsync(() -> {
                        BalanceUpdateEvent event = new BalanceUpdateEvent();
                        event.setBalance(giftAmount);
                        event.setType("CREDIT");
                        event.setUserId(userId);
                        event.setWalletId(giftCodeRedeemEvent.getCreditWalletId());
                        event.setCcy(giftCodeCcy);
                        rabbitTemplate.convertAndSend(balanceUpdateExchange, balanceUpdateRoutingKey, event);
                    });
                }
            }

            RedeemGiftCodeResponseDto redeemGiftCodeResponseDto = new RedeemGiftCodeResponseDto();
            redeemGiftCodeResponseDto.setGiftCode(giftCodeValidationResponseDto.getGiftCode());
            redeemGiftCodeResponseDto.setStatus("REDEEMED");
            return redeemGiftCodeResponseDto;
        }

        RedeemGiftCodeResponseDto redeemGiftCodeResponseDto = new RedeemGiftCodeResponseDto();
        redeemGiftCodeResponseDto.setDescription("Invalid gift code");
        return redeemGiftCodeResponseDto;
    }

    private String generateUniqueCode() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(CODE_CHARS.charAt(random.nextInt(CODE_CHARS.length())));
        }
        String code = sb.toString();
        // Ensure the generated code is unique in the database (though probability of collision is low with this length)
        if (giftCodeRepository.findByGiftCode(code).isPresent()) {
            return generateUniqueCode(); // Recursively generate if it exists
        }
        return code;
    }
}
