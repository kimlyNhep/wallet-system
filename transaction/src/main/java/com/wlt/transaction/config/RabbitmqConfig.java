package com.wlt.transaction.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitmqConfig {
    @Value("${rabbitmq.exchange.user}")
    private String userExchange;

    @Value("${rabbitmq.queue.wallet-creation}")
    private String walletCreationQueue;

    @Value("${rabbitmq.routing-key.user-created}")
    private String userCreatedRoutingKey;

    @Bean
    public Exchange userExchange() {
        return ExchangeBuilder.topicExchange(userExchange).durable(true).build();
    }

    @Bean
    public Queue walletCreationQueue() {
        return QueueBuilder.durable(walletCreationQueue).build();
    }

    @Bean
    public Binding walletCreationBinding(Queue walletCreationQueue, Exchange userExchange) {
        return BindingBuilder.bind(walletCreationQueue).to(userExchange).with(userCreatedRoutingKey).noargs();
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
