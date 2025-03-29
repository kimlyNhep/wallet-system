package com.wlt.wallet.consumer;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class WalletUpdateConsumer {

    @RabbitListener(queues = "${rabbitmq.queue.wallet}")
    public void handleUpdateWalletAmount() {

    }
}
