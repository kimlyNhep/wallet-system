package com.wlt.transaction.consumer;

import com.wlt.transaction.dto.TransactionHistoryEvent;
import com.wlt.transaction.service.TransactionHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionHistoryConsumer {

    public final TransactionHistoryService transactionHistoryService;

    @RabbitListener(queues = "${rabbitmq.queue.transaction.history}")
    public void transactionHistoryConsumer(TransactionHistoryEvent transactionHistoryEvent) {
        try {
            transactionHistoryService.saveTransactionHistory(transactionHistoryEvent);
        } catch (Exception e) {
            log.info("transaction history consumer error", e);
        }
    }
}
