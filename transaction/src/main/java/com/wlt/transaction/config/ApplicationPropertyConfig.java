package com.wlt.transaction.config;

import org.springframework.beans.factory.annotation.Value;

public class ApplicationPropertyConfig {
    @Value("${rabbitmq.exchange.transaction.history}")
    protected String transactionHistoryExchange;

    @Value("${rabbitmq.queue.transaction.history}")
    protected String transactionHistoryQueue;

    @Value("${rabbitmq.routing-key.transaction.history}")
    protected String transactionHistoryRoutingKey;
}
