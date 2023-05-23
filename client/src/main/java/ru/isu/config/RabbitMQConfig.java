package ru.isu.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static ru.isu.model.RabbitQueue.*;


@Configuration
public class RabbitMQConfig {

    /**
     * convert update to json then send to rabbitmq
     * when getting json then convert json to javaObject
     * @return
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public Queue textMessageQueue() {
        return new Queue(TEXT_MESSAGE);
    }

    @Bean
    public Queue docMessageQueue() {
        return new Queue(DOC_MESSAGE);
    }

    @Bean
    public Queue answerMessageQueue() {
        return new Queue(ANSWER_MESSAGE);
    }

    @Bean
    public Queue validMessageQueue() {
        return new Queue(VALID_MESSAGE);
    }
    @Bean
    public Queue webMessageQueue() {
        return new Queue(WEB_MESSAGE);
    }
    @Bean
    public Queue webAnswerMessageQueue() {
        return new Queue(WEB_ANSWER_MESSAGE);
    }
}
