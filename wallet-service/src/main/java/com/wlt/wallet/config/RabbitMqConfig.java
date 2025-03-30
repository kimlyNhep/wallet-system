package com.wlt.wallet.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig extends ApplicationPropertyConfig {

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
    public Exchange balanceUpdateExchange() {
        return ExchangeBuilder.topicExchange(balanceUpdateExchange).durable(true).build();
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
    public Queue balanceUpdateQueue() {
        return QueueBuilder.durable(balanceUpdateQueue).build();
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
    public Binding balanceUpdateBinding(Queue balanceUpdateQueue, Exchange balanceUpdateExchange) {
        return BindingBuilder
                .bind(balanceUpdateQueue)
                .to(balanceUpdateExchange)
                .with(balanceUpdateRoutingKey)
                .noargs();
    }

    @Bean
    public Jackson2JsonMessageConverter producerJackson2MessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(producerJackson2MessageConverter());
        return rabbitTemplate;
    }
}
