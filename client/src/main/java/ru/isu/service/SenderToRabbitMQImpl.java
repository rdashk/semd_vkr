package ru.isu.service;

import org.jvnet.hk2.annotations.Service;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import ru.isu.model.Answer;

@Service
@Component
public class SenderToRabbitMQImpl implements SenderToRabbitMQ{

    private RabbitTemplate rabbitTemplate;

    public SenderToRabbitMQImpl(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void send(String rabbitMQ, Answer answer) {
        rabbitTemplate.convertAndSend(rabbitMQ, answer);
    }
}
