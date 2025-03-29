package com.wlt.payment.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {
    @Value("${rabbitmq.exchange.gift-code}")
    private String giftCodeExchange;

    @Value("${rabbitmq.queue.gift-code.redeem}")
    private String redeemGiftCodeQueue;

    @Value("${rabbitmq.routing-key.gift-code.redeem}")
    private String redeemGiftCodeRoutingKey;

    @Value("${rabbitmq.exchange.fund-transfer}")
    private String fundTransferExchange;

    @Value("${rabbitmq.routing-key.fund-transfer}")
    private String fundTransferRoutingKey;

    @Value("${rabbitmq.queue.fund-transfer}")
    private String fundTransferQueue;

    @Value("${rabbitmq.exchange.wallet}")
    private String walletExchange;

    @Value("${rabbitmq.routing-key.wallet}")
    private String walletUpdateRoutingKey;

    @Value("${rabbitmq.queue.wallet}")
    private String walletUpdateQueue;

    @Bean
    public Exchange fundTransferExchange() {
        return ExchangeBuilder.topicExchange(fundTransferExchange).durable(true).build();
    }

    @Bean
    public Queue fundTransferQueue() {
        return QueueBuilder.durable(fundTransferQueue).build();
    }

    @Bean
    public Exchange walletUpdateExchange() {
        return ExchangeBuilder.topicExchange(walletExchange).durable(true).build();
    }

    @Bean
    public Queue walletUpdateQueue() {
        return QueueBuilder.durable(walletUpdateQueue).build();
    }

    @Bean
    public Exchange giftCodeExchange() {
        return ExchangeBuilder.topicExchange(giftCodeExchange).durable(true).build();
    }

    @Bean
    public Queue redeemGiftCodeQueue() {
        return QueueBuilder.durable(redeemGiftCodeQueue).build();
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
