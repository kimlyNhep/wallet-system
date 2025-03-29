package com.wlt.payment.service.impl;

import com.wlt.payment.constants.Status;
import com.wlt.payment.dto.*;
import com.wlt.payment.entity.ExchangeRate;
import com.wlt.payment.entity.GiftCode;
import com.wlt.payment.entity.Transaction;
import com.wlt.payment.repository.ExchangeRateRepository;
import com.wlt.payment.repository.GiftCodeRepository;
import com.wlt.payment.repository.TransactionRepository;
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
    private final ExchangeRateRepository exchangeRateRepository;

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
}
