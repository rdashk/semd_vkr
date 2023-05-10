package ru.isu.service;

import org.jvnet.hk2.annotations.Service;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.telegram.telegrambots.meta.api.objects.Update;

@Service
public class SenderToRabbitMQImpl implements SenderToRabbitMQ{

    private RabbitTemplate rabbitTemplate;

    public SenderToRabbitMQImpl(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void send(String rabbitMQ, Update update) {
        rabbitTemplate.convertAndSend(rabbitMQ, update);
    }
}
