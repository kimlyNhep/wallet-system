package com.wlt.wallet.service.impl;

import com.wlt.wallet.config.ApplicationPropertyConfig;
import com.wlt.wallet.constants.Status;
import com.wlt.wallet.dto.*;
import com.wlt.wallet.service.AccountService;
import com.wlt.wallet.service.balanceUpdateService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class BalanceUpdateServiceImpl extends ApplicationPropertyConfig implements balanceUpdateService {

    private final AccountService accountService;
    private final RabbitTemplate rabbitTemplate;

    @Override
    public void updateBalance(BalanceUpdateEvent event) {
        if (event.getType().equals("CREDIT")) {
            CreditAccountBalanceRequestDto requestDto = new CreditAccountBalanceRequestDto();
            requestDto.setCreditBalance(event.getBalance());
            requestDto.setCcy(event.getCcy());
            requestDto.setCreditWalletId(event.getWalletId());
            AccountBalanceResponseDto balanceResponseDto = accountService.creditAccountBalance(event.getUserId(), requestDto);

            String tranRefNo = UUID.randomUUID().toString();
            CompletableFuture.runAsync(() -> {
                TransactionHistoryEvent transactionHistoryEvent = new TransactionHistoryEvent();
                transactionHistoryEvent.setCrAmount(balanceResponseDto.getBalance());
                transactionHistoryEvent.setCrCcy(balanceResponseDto.getCcy());
                transactionHistoryEvent.setTransactionRefNo(tranRefNo);
                transactionHistoryEvent.setStatus(Status.SUCCESS);
                transactionHistoryEvent.setExchangeRate(balanceResponseDto.getExchangeRate());
                transactionHistoryEvent.setAmount(event.getBalance());
                transactionHistoryEvent.setTxnCcy(event.getCcy());
                transactionHistoryEvent.setSaveTimestamp(LocalDateTime.now());
                transactionHistoryEvent.setUserId(event.getUserId());
                transactionHistoryEvent.setCrWalletId(event.getWalletId());
                transactionHistoryEvent.setTransactionType("REDEEM_GIFT_CODE");
                rabbitTemplate.convertAndSend(transactionHistoryExchange, transactionHistoryRoutingKey, transactionHistoryEvent);
            });
        }
    }
}
