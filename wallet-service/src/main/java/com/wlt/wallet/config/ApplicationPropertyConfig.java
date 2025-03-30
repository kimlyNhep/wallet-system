package com.wlt.wallet.config;

import org.springframework.beans.factory.annotation.Value;

public class ApplicationPropertyConfig {
    @Value("${rabbitmq.exchange.user}")
    protected String userExchange;

    @Value("${rabbitmq.exchange.gift-code}")
    protected String giftCodeExchange;

    @Value("${rabbitmq.queue.wallet-creation}")
    protected String walletCreationQueue;

    @Value("${rabbitmq.queue.gift-code.redeem}")
    protected String redeemGiftCodeQueue;

    @Value("${rabbitmq.routing-key.user-created}")
    protected String userCreatedRoutingKey;

    @Value("${rabbitmq.routing-key.gift-code.redeem}")
    protected String redeemGiftCodeRoutingKey;

    @Value("${rabbitmq.exchange.fund-transfer}")
    protected String fundTransferExchange;

    @Value("${rabbitmq.routing-key.fund-transfer}")
    protected String fundTransferRoutingKey;

    @Value("${rabbitmq.queue.fund-transfer}")
    protected String fundTransferQueue;

    @Value("${rabbitmq.queue.init.fund-transfer}")
    protected String initFundTransferQueue;

    @Value("${rabbitmq.routing-key.init.fund-transfer}")
    protected String initFundTransferRoutingKey;

    @Value("${rabbitmq.exchange.wallet}")
    protected String walletExchange;

    @Value("${rabbitmq.routing-key.wallet}")
    protected String walletUpdateRoutingKey;

    @Value("${rabbitmq.queue.wallet}")
    protected String walletUpdateQueue;

    @Value("${rabbitmq.exchange.balance-update}")
    protected String balanceUpdateExchange;
    @Value("${rabbitmq.routing-key.balance-update}")
    protected String balanceUpdateRoutingKey;
    @Value("${rabbitmq.queue.balance-update}")
    protected String balanceUpdateQueue;

    @Value("${rabbitmq.exchange.transaction.history}")
    protected String transactionHistoryExchange;

    @Value("${rabbitmq.queue.transaction.history}")
    protected String transactionHistoryQueue;

    @Value("${rabbitmq.routing-key.transaction.history}")
    protected String transactionHistoryRoutingKey;
}
