package com.wlt.payment.service.impl;

import com.wlt.payment.dto.*;
import com.wlt.payment.entity.ExchangeRate;
import com.wlt.payment.entity.GiftCode;
import com.wlt.payment.repository.ExchangeRateRepository;
import com.wlt.payment.repository.GiftCodeRepository;
import com.wlt.payment.service.PaymentService;
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
public class PaymentServiceImpl implements PaymentService {

    @Value("${wallet.service.base-url}")
    private String walletServiceUrl;

    @Value("${rabbitmq.exchange.wallet}")
    private String walletExchange;

    @Value("${rabbitmq.routing-key.wallet}")
    private String walletUpdateRoutingKey;

    @Value("${rabbitmq.queue.wallet}")
    private String walletUpdateQueue;

    private final GiftCodeRepository giftCodeRepository;
    private final RestTemplate restTemplate;
    private final RabbitTemplate rabbitTemplate;

    private static final String CODE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 12;
    private final ExchangeRateRepository exchangeRateRepository;

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
    public GetExchangeRateAmountDto getExchangeRateAmount(GetExchangeRateAmountRequestDto getExchangeRateAmountRequestDto) {
        Optional<ExchangeRate> exchangeRateOptional = exchangeRateRepository.findByCrCcyAndDrCcy(getExchangeRateAmountRequestDto.getCrCcy(), getExchangeRateAmountRequestDto.getDrCcy());
        if (exchangeRateOptional.isEmpty()) {
            exchangeRateOptional = exchangeRateRepository.findByCrCcyAndDrCcy(getExchangeRateAmountRequestDto.getDrCcy(), getExchangeRateAmountRequestDto.getCrCcy());
        }

        GetExchangeRateAmountDto responseDto = new GetExchangeRateAmountDto();
        if (exchangeRateOptional.isPresent()) {
            ExchangeRate exchangeRate = exchangeRateOptional.get();
            if (exchangeRate.getDrCcy().equalsIgnoreCase(getExchangeRateAmountRequestDto.getCrCcy()) && exchangeRate.getCrCcy().equalsIgnoreCase(getExchangeRateAmountRequestDto.getDrCcy())) {
                 responseDto.setRate(BigDecimal.ONE.divide(BigDecimal.valueOf(exchangeRate.getRate()), 10, java.math.RoundingMode.HALF_UP));
            } else {
                BigDecimal rate = BigDecimal.valueOf(exchangeRate.getRate());
                responseDto.setRate(rate);
            }
            return responseDto;
        } else {
            throw new RuntimeException("ExchangeRate not found");
        }
    }

    @Override
    public FundTransferResponseDto fundTransfer(Long userId, FundTransferRequestDto fundTransferRequestDto) {
        MultiValueMap<String, String> headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        headers.add("Collation-id", UUID.randomUUID().toString());

        if (fundTransferRequestDto.getCrCcy().equalsIgnoreCase(fundTransferRequestDto.getDrCcy())) {
           ResponseEntity<SuccessResponse<WalletAccountResponseDto>> drWalletResponseEntity = restTemplate.exchange(
                   walletServiceUrl + "/api/v1/account/" + fundTransferRequestDto.getDrWalletId(),
                   HttpMethod.GET,
                   new HttpEntity<>(null, headers),
                   new ParameterizedTypeReference<>() {
                   }
           );

           if (drWalletResponseEntity.getStatusCode().is2xxSuccessful()) {
               SuccessResponse<WalletAccountResponseDto> drWalletResponse = drWalletResponseEntity.getBody();
               if (drWalletResponse != null && drWalletResponse.getData() != null) {
                   BigDecimal debitAmount = drWalletResponse.getData().getBalance();
                   if (debitAmount.compareTo(fundTransferRequestDto.getAmount()) > 0) {
                       BigDecimal newDebitAmount = debitAmount.subtract(fundTransferRequestDto.getAmount());

                       CompletableFuture.runAsync(() -> {
                           WalletUpdateEvent walletUpdateEvent = new WalletUpdateEvent();
                           walletUpdateEvent.setType("debit");
                           walletUpdateEvent.setAmount(newDebitAmount);
                           walletUpdateEvent.setWalletId(drWalletResponse.getData().getId());
                           walletUpdateEvent.setUserId(userId);
                           walletUpdateEvent.setCcy(fundTransferRequestDto.getCcy());
                           rabbitTemplate.convertAndSend(walletExchange, walletUpdateRoutingKey, walletUpdateEvent);
                       });
                   }
               }
           }
       } else {
            // cross ccy
        }

        FundTransferResponseDto responseDto = new FundTransferResponseDto();
        return responseDto;
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
