package com.wlt.payment.consumer;

import com.wlt.payment.dto.GiftCodeRedeemEvent;
import com.wlt.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class GiftCodeConsumer {

    private final PaymentService paymentService;

    @RabbitListener(queues = "${rabbitmq.queue.gift-code.redeem}")
    public void handleUserCreatedEvent(GiftCodeRedeemEvent event) {
        Long userId = event.getUserId();
        String giftCode = event.getGiftCode();
        paymentService.markGiftCodeAsRedeemed(userId, giftCode);
    }
}
