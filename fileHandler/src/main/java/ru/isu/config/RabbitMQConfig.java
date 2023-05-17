package ru.isu.config;

import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Only set bean converter
 * queues are existed
 */
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

    /*@Bean
    public RecipientService getRecipientService(RabbitTemplate r) {
        return new RecipientServiceImpl(new SenderServiceImpl(r), new FileController(), semdRepository);
    }*/
}
