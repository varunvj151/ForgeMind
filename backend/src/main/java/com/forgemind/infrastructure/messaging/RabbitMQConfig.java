package com.forgemind.infrastructure.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "app.eventbus.provider", havingValue = "rabbitmq")
public class RabbitMQConfig {

  public static final String EVENTS_QUEUE = "forgemind.events.queue";
  public static final String DLQ = "forgemind.events.dlq";

  @Bean
  public TopicExchange eventsExchange() {
    return new TopicExchange(RabbitMQEventBus.EXCHANGE, true, false);
  }

  @Bean
  public Queue eventsQueue() {
    return org.springframework.amqp.core.QueueBuilder.durable(EVENTS_QUEUE)
        .withArgument("x-dead-letter-exchange", "")
        .withArgument("x-dead-letter-routing-key", DLQ)
        .build();
  }

  @Bean
  public Queue deadLetterQueue() {
    return new Queue(DLQ, true);
  }

  @Bean
  public Binding binding(Queue eventsQueue, TopicExchange eventsExchange) {
    return BindingBuilder.bind(eventsQueue).to(eventsExchange).with("#");
  }

  @Bean
  public MessageConverter jsonMessageConverter() {
    return new Jackson2JsonMessageConverter();
  }
}
