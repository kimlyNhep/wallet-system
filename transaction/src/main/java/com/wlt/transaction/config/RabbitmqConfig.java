package com.wlt.transaction.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitmqConfig extends ApplicationPropertyConfig{

    @Bean
    public Exchange transactionHistoryExchange() {
        return ExchangeBuilder.topicExchange(transactionHistoryExchange).durable(true).build();
    }

    @Bean
    public Queue transactionHistoryQueue() {
        return QueueBuilder.durable(transactionHistoryQueue).build();
    }

    @Bean
    public Binding transactionHistoryBinding(Queue transactionHistoryQueue, Exchange transactionHistoryExchange) {
        return BindingBuilder.bind(transactionHistoryQueue)
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
