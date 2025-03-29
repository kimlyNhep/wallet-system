package com.wlt.payment.service.impl;

import com.wlt.payment.dto.*;
import com.wlt.payment.entity.GiftCode;
import com.wlt.payment.repository.GiftCodeRepository;
import com.wlt.payment.service.PaymentService;
import com.wlt.payment.service.TopUpBalanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
public class TopUpBalanceServiceImpl implements TopUpBalanceService {

    @Value("${wallet.service.base-url}")
    private String walletServiceUrl;
    private final RestTemplate restTemplate;

    private final GiftCodeRepository giftCodeRepository;
    private final PaymentService paymentService;

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
    public void redeemGiftCode(Long userId, GiftCodeRedeemEvent giftCodeRedeemEvent) {
//        try {
            GiftCodeValidationResponseDto giftCodeValidationResponseDto = validateGiftCode(giftCodeRedeemEvent.getGiftCode());
            if (giftCodeValidationResponseDto.isValid()) {
                BigDecimal giftAmount = giftCodeValidationResponseDto.getAmount();
                String giftCodeCcy = giftCodeValidationResponseDto.getCcy();

                MultiValueMap<String, String> headers = new HttpHeaders();
                headers.add("Content-Type", "application/json");
                headers.add("Collation-id", UUID.randomUUID().toString());

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
                        WalletAccountResponseDto walletAccountResponseDto = crWalletResponseEntityBody.getData();
                        if (!walletAccountResponseDto.getCcy().equalsIgnoreCase(giftCodeCcy)) {
                            GetExchangeRateAmountRequestDto exchangeRateAmountRequestDto = new GetExchangeRateAmountRequestDto();
                            exchangeRateAmountRequestDto.setCrCcy(crWalletResponseEntityBody.getData().getCcy());
                            exchangeRateAmountRequestDto.setDrCcy(giftCodeCcy);
                            GetExchangeRateAmountDto exchangeRateResponseDto = paymentService.getExchangeRateAmount(exchangeRateAmountRequestDto);
                            BigDecimal balance = giftAmount.multiply(exchangeRateResponseDto.getRate());
//                            BigDecimal newBalance = crWalletResponseEntityBody.getData().getBalance().add(balance);
                            // call credit balance
                        }
                    }
                }
            }

//            if (giftCodeValidationResponse.getStatusCode().is2xxSuccessful()) {
//                SuccessResponse<GiftCodeValidationResponseDto> responseBody = giftCodeValidationResponse.getBody();
//                if (responseBody != null && responseBody.getData() != null) {
//
//                    if (!responseBody.getData().isValid()) {
//                        RedeemGiftCodeResponseDto response = new RedeemGiftCodeResponseDto();
//                        response.setDescription("Gift code validation failed");
//                        return response;
//                    }
//
//                    BigDecimal giftAmount = responseBody.getData().getAmount();
//                    String ccy = responseBody.getData().getCcy();
//
//                    Optional<WalletAccount> walletAccountOptional = walletAccountRepository.findByIdAndStatus(requestDto.getCreditWalletId(), CommonConstants.ACTIVE);
//                    if (walletAccountOptional.isPresent()) {
//                        WalletAccount walletAccount = walletAccountOptional.get();
//                        if (giftAmount.compareTo(BigDecimal.ZERO) > 0) {
//                            if (!walletAccount.getCcy().equals(ccy)) {
//                                GetExchangeRateRequestDto getExchangeRateRequestDto = new GetExchangeRateRequestDto();
//                                getExchangeRateRequestDto.setCrCcy(walletAccount.getCcy());
//                                getExchangeRateRequestDto.setDrCcy(ccy);
//
//                                ResponseEntity<SuccessResponse<GetExchangeRateResponseDto>> exchangeRateEntity = restTemplate.exchange(
//                                        paymentServiceBaseUrl + "/api/v1/exchange-rate",
//                                        HttpMethod.POST,
//                                        new HttpEntity<>(getExchangeRateRequestDto, headers),
//                                        new ParameterizedTypeReference<>() {
//                                        });
//
//                                if (exchangeRateEntity.getStatusCode().is2xxSuccessful()) {
//                                    SuccessResponse<GetExchangeRateResponseDto> rate = exchangeRateEntity.getBody();
//                                    if (rate != null && rate.getData() != null) {
//                                        GetExchangeRateResponseDto exchangeRateResponseDto = rate.getData();
//                                        BigDecimal balance = giftAmount.multiply(exchangeRateResponseDto.getRate());
//                                        BigDecimal newBalance = walletAccount.getBalance().add(balance);
//                                        walletAccount.setBalance(newBalance);
//                                        walletAccountRepository.save(walletAccount);
//                                    }
//                                }
//                            }
//                            else {
//                                BigDecimal balance = walletAccount.getBalance().add(giftAmount);
//                                walletAccount.setBalance(balance);
//                                walletAccountRepository.save(walletAccount);
//                            }
//                            // record transaction
//
//                            // update gift code to redeem
//                            CompletableFuture.runAsync(() -> {
//                                GiftCodeRedeemEvent giftCodeRedeemEvent = new GiftCodeRedeemEvent();
//                                giftCodeRedeemEvent.setUserId(userId);
//                                giftCodeRedeemEvent.setGiftCode(requestDto.getGiftCode());
//                                rabbitTemplate.convertAndSend(giftCodeExchange, redeemGiftCodeRoutingKey, giftCodeRedeemEvent);
//                            });
//
//                            RedeemGiftCodeResponseDto response = new RedeemGiftCodeResponseDto();
//                            response.setDescription("Gift code redeemed successfully. Wallet credited with" + giftAmount + " \"" + ccy);
//                            return response;
//                        } else {
//                            RedeemGiftCodeResponseDto response = new RedeemGiftCodeResponseDto();
//                            response.setDescription("Couldn't redeem gift code");
//                            return response;
//                        }
//                    } else {
//                        RedeemGiftCodeResponseDto response = new RedeemGiftCodeResponseDto();
//                        response.setDescription("Wallet account not found");
//                    }
//                }
//            }
//        } catch (Exception e) {
//            throw new RuntimeException("Failed to get redeem gift code", e);
//        }
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
