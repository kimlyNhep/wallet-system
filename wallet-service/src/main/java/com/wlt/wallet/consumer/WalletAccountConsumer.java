package com.wlt.wallet.consumer;

import com.wlt.wallet.dto.UserCreatedEvent;
import com.wlt.wallet.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class WalletAccountConsumer {

//    private final AccountService walletService;
//
//    @RabbitListener(queues = "${rabbitmq.queue.wallet-creation}")
//    public void handleUserCreatedEvent(UserCreatedEvent event) {
//        Long userId = event.getUserId();
//        walletService.createWallet(userId, , BigDecimal.ZERO);
//    }
}
