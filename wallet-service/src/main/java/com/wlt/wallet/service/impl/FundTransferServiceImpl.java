package com.wlt.wallet.service.impl;

import com.wlt.wallet.constants.CommonConstants;
import com.wlt.wallet.constants.MessageError;
import com.wlt.wallet.dto.*;
import com.wlt.wallet.entity.WalletAccount;
import com.wlt.wallet.exception.CustomException;
import com.wlt.wallet.repository.WalletAccountRepository;
import com.wlt.wallet.service.FundTransferService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
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

    @Value("${rabbitmq.routing-key.init.fund-transfer}")
    private String initFundTransferRoutingKey;

    private final WalletAccountRepository accountRepository;
    private final RabbitTemplate rabbitTemplate;

    @Override
    public FundTransferResponseDto initFundTransfer(Long userId, InitFundTransferRequestDto fundTransferRequestDto) {
        Optional<WalletAccount> sourceWalletAccount = accountRepository.findByIdAndStatus(fundTransferRequestDto.getDrWalletId(), CommonConstants.ACTIVE);
        if (sourceWalletAccount.isPresent()) {
            BigDecimal sourceBalance = sourceWalletAccount.get().getBalance();

            if (!userId.equals(sourceWalletAccount.get().getUserId())) {
                throw new CustomException(MessageError.CANNOT_ACCESS_OTHER_WALLET);
            }

            if (!sourceWalletAccount.get().getCcy().equals(fundTransferRequestDto.getDrCcy())) {
                throw new CustomException(MessageError.INVALID_CCY_CODE);
            }

            if (sourceBalance.compareTo(fundTransferRequestDto.getAmount()) < 0) {
                throw new CustomException(MessageError.ERR_001_INSUFFICIENT_AMOUNT);
            }
        } else {
            throw new CustomException(MessageError.ERR_002_WALLET_NOT_ENOUGH);
        }

        Optional<WalletAccount> targetWalletAccount = accountRepository.findByIdAndStatus(fundTransferRequestDto.getCrWalletId(), CommonConstants.ACTIVE);
        if (targetWalletAccount.isEmpty()) {
            throw new CustomException(MessageError.ERR_002_WALLET_NOT_ENOUGH);
        }

        WalletAccount targetWallet = targetWalletAccount.get();
        if (!targetWallet.getCcy().equals(fundTransferRequestDto.getCrCcy())) {
            throw new CustomException(MessageError.INVALID_CCY_CODE);
        }

        // process payment
        String paymentRefNo = UUID.randomUUID().toString();
        CompletableFuture.runAsync(() -> {
            FundTransferEvent fundTransferEvent = new FundTransferEvent();
            fundTransferEvent.setAmount(fundTransferRequestDto.getAmount());
            fundTransferEvent.setCcy(fundTransferRequestDto.getCcy());
            fundTransferEvent.setDrCcy(fundTransferRequestDto.getDrCcy());
            fundTransferEvent.setCrCcy(fundTransferRequestDto.getCrCcy());
            fundTransferEvent.setDrWalletId(fundTransferRequestDto.getDrWalletId());
            fundTransferEvent.setCrWalletId(fundTransferRequestDto.getCrWalletId());
            fundTransferEvent.setUserId(userId);
            fundTransferEvent.setPaymentRefNo(paymentRefNo);
            rabbitTemplate.convertAndSend(fundTransferExchange, initFundTransferRoutingKey, fundTransferEvent);
        });

        FundTransferResponseDto response = new FundTransferResponseDto();
        response.setPaymentRefNo(paymentRefNo);
        response.setCrWalletId(fundTransferRequestDto.getCrWalletId());
        response.setDrWalletId(fundTransferRequestDto.getDrWalletId());
        response.setCrCcy(fundTransferRequestDto.getCrCcy());
        response.setDrCcy(fundTransferRequestDto.getDrCcy());
        response.setCcy(fundTransferRequestDto.getCcy());
        response.setAmount(fundTransferRequestDto.getAmount());
        response.setMessage("Your fund transfer has been initiated.");
        return response;
    }
}
