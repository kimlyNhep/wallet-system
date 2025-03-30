package com.wlt.wallet.consumer;

import com.wlt.wallet.dto.BalanceUpdateEvent;
import com.wlt.wallet.dto.CreditAccountBalanceRequestDto;
import com.wlt.wallet.dto.DebitAccountBalanceRequestDto;
import com.wlt.wallet.service.AccountService;
import com.wlt.wallet.service.balanceUpdateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BalanceUpdateConsumer {

    private final balanceUpdateService balanceUpdateService;

    @RabbitListener(queues = "${rabbitmq.queue.balance-update}")
    public void handleBalanceUpdate(BalanceUpdateEvent event) {
        try {
            balanceUpdateService.updateBalance(event);
        } catch (Exception e) {
            log.info(e.getMessage());
        }
    }
}
