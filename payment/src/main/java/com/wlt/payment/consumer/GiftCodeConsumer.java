package com.wlt.payment.consumer;

import com.wlt.payment.dto.GiftCodeRedeemEvent;
import com.wlt.payment.service.PaymentService;
import com.wlt.payment.service.TopUpBalanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class GiftCodeConsumer {

    private final TopUpBalanceService topUpBalanceService;

    @RabbitListener(queues = "${rabbitmq.queue.gift-code.redeem}")
    public void handleRedeemGiftCode(GiftCodeRedeemEvent event) {
        Long userId = event.getUserId();
        topUpBalanceService.redeemGiftCode(userId, event);
    }
}
