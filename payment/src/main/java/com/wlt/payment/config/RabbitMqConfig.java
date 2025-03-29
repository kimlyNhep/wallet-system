package com.wlt.payment.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig extends ApplicationPropertyConfig {
    @Bean
    public Exchange fundTransferExchange() {
        return ExchangeBuilder.topicExchange(fundTransferExchange).durable(true).build();
    }

    @Bean
    public Exchange walletUpdateExchange() {
        return ExchangeBuilder.topicExchange(walletExchange).durable(true).build();
    }

    @Bean
    public Exchange transactionHistoryExchange() {
        return ExchangeBuilder.topicExchange(transactionHistoryExchange).durable(true).build();
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
    public Queue transactionHistoryQueue() {
        return QueueBuilder.durable(transactionHistoryQueue).build();
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
    public Binding transactionHistoryBinding(Queue transactionHistoryQueue, Exchange transactionHistoryExchange) {
        return BindingBuilder
                .bind(transactionHistoryQueue)
                .to(transactionHistoryExchange)
                .with(transactionHistoryRoutingKey)
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
