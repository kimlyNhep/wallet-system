package com.wlt.wallet.service.impl;

import com.wlt.wallet.constants.CommonConstants;
import com.wlt.wallet.dto.FundTransferEvent;
import com.wlt.wallet.dto.InitFundTransferRequestDto;
import com.wlt.wallet.dto.FundTransferResponseDto;
import com.wlt.wallet.entity.WalletAccount;
import com.wlt.wallet.repository.WalletAccountRepository;
import com.wlt.wallet.service.FundTransferService;
import com.wlt.wallet.service.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class FundTransferServiceImpl implements FundTransferService {

    @Value("${rabbitmq.exchange.fund-transfer}")
    private String fundTransferExchange;

    @Value("${rabbitmq.routing-key.fund-transfer}")
    private String fundTransferRoutingKey;

    @Value("${rabbitmq.queue.fund-transfer}")
    private String fundTransferQueue;

    private final RabbitTemplate rabbitTemplate;
    private final WalletAccountRepository accountRepository;
    private final RedisService redisService;

    @Override
    public FundTransferResponseDto fundTransfer(Long userId, InitFundTransferRequestDto fundTransferRequestDto) {
        Optional<WalletAccount> sourceWalletAccount = accountRepository.findByIdAndStatus(fundTransferRequestDto.getDrWalletId(), CommonConstants.ACTIVE);
        if (sourceWalletAccount.isPresent()) {
            BigDecimal sourceBalance = sourceWalletAccount.get().getBalance();
            if (sourceBalance.compareTo(fundTransferRequestDto.getAmount()) < 0) {
                throw new RuntimeException("Insufficient amount");
            }
        } else {
            throw new RuntimeException("source wallet not found");
        }

        Optional<WalletAccount> targetWalletAccount = accountRepository.findByIdAndStatus(fundTransferRequestDto.getCrWalletId(), CommonConstants.ACTIVE);
        if (targetWalletAccount.isEmpty()) {
            throw new RuntimeException("target wallet not found");
        }

        String paymentRefNo = UUID.randomUUID().toString();
        CompletableFuture.runAsync(() -> {
            FundTransferEvent fundTransferResponseDto = new FundTransferEvent();
            BeanUtils.copyProperties(fundTransferRequestDto, fundTransferResponseDto);
            fundTransferResponseDto.setPaymentRefNo(paymentRefNo);
            fundTransferResponseDto.setUserId(userId);
            rabbitTemplate.convertAndSend(fundTransferExchange, fundTransferRoutingKey, fundTransferResponseDto);
        });

        FundTransferResponseDto response = new FundTransferResponseDto();
        response.setPaymentRefNo(paymentRefNo);
        return response;
    }
}
