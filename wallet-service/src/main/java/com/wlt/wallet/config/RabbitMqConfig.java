package com.wlt.wallet.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {
    @Value("${rabbitmq.exchange.user}")
    private String userExchange;

    @Value("${rabbitmq.exchange.gift-code}")
    private String giftCodeExchange;

    @Value("${rabbitmq.queue.wallet-creation}")
    private String walletCreationQueue;

    @Value("${rabbitmq.queue.gift-code.redeem}")
    private String redeemGiftCodeQueue;

    @Value("${rabbitmq.routing-key.user-created}")
    private String userCreatedRoutingKey;

    @Value("${rabbitmq.routing-key.gift-code.redeem}")
    private String redeemGiftCodeRoutingKey;

    @Value("${rabbitmq.exchange.fund-transfer}")
    private String fundTransferExchange;

    @Value("${rabbitmq.routing-key.fund-transfer}")
    private String fundTransferRoutingKey;

    @Value("${rabbitmq.queue.fund-transfer}")
    private String fundTransferQueue;

    @Value("${rabbitmq.queue.init.fund-transfer}")
    private String initFundTransferQueue;

    @Value("${rabbitmq.routing-key.init.fund-transfer}")
    private String initFundTransferRoutingKey;

    @Value("${rabbitmq.exchange.wallet}")
    private String walletExchange;

    @Value("${rabbitmq.routing-key.wallet}")
    private String walletUpdateRoutingKey;

    @Value("${rabbitmq.queue.wallet}")
    private String walletUpdateQueue;

    @Bean
    public Exchange userExchange() {
        return ExchangeBuilder.topicExchange(userExchange).durable(true).build();
    }

    @Bean
    public Exchange giftCodeExchange() {
        return ExchangeBuilder.topicExchange(giftCodeExchange).durable(true).build();
    }

    @Bean
    public Exchange fundTransferExchange() {
        return ExchangeBuilder.topicExchange(fundTransferExchange).durable(true).build();
    }

    @Bean
    public Exchange walletUpdateExchange() {
        return ExchangeBuilder.topicExchange(walletExchange).durable(true).build();
    }

    @Bean
    public Queue walletCreationQueue() {
        return QueueBuilder.durable(walletCreationQueue).build();
    }

    @Bean
    public Queue redeemGiftCodeQueue() {
        return QueueBuilder.durable(redeemGiftCodeQueue).build();
    }

    @Bean
    public Queue fundTransferQueue() {
        return QueueBuilder.durable(fundTransferQueue).build();
    }

    @Bean
    public Queue walletUpdateQueue() {
        return QueueBuilder.durable(walletUpdateQueue).build();
    }

    @Bean
    public Queue initFundTransferQueue() {
        return QueueBuilder.durable(initFundTransferQueue).build();
    }

    @Bean
    public Binding walletCreationBinding(Queue walletCreationQueue, Exchange userExchange) {
        return BindingBuilder
                .bind(walletCreationQueue)
                .to(userExchange)
                .with(userCreatedRoutingKey)
                .noargs();
    }

    @Bean
    public Binding redeemGiftCodeBinding(Queue redeemGiftCodeQueue, Exchange giftCodeExchange) {
        return BindingBuilder
                .bind(redeemGiftCodeQueue)
                .to(giftCodeExchange)
                .with(redeemGiftCodeRoutingKey)
                .noargs();
    }

    @Bean
    public Binding fundTransferBinding(Queue fundTransferQueue, Exchange fundTransferExchange) {
        return BindingBuilder
                .bind(fundTransferQueue)
                .to(fundTransferExchange)
                .with(fundTransferRoutingKey)
                .noargs();
    }

    @Bean
    public Binding walletUpdateBinding(Queue walletUpdateQueue, Exchange walletUpdateExchange) {
        return BindingBuilder
                .bind(walletUpdateQueue)
                .to(walletUpdateExchange)
                .with(walletUpdateRoutingKey)
                .noargs();
    }

    @Bean
    public Binding initFundTransferBinding(Queue initFundTransferQueue, Exchange fundTransferExchange) {
        return BindingBuilder
                .bind(initFundTransferQueue)
                .to(fundTransferExchange)
                .with(initFundTransferRoutingKey)
                .noargs();
    }

    @Bean
    public Jackson2JsonMessageConverter producerJackson2MessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(org.springframework.amqp.rabbit.connection.ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(producerJackson2MessageConverter());
        return rabbitTemplate;
    }
}
