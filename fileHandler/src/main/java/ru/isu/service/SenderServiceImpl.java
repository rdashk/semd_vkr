package ru.isu.service;

import org.jvnet.hk2.annotations.Service;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import ru.isu.model.Answer;

/**
 * Install listener for every queue in RabbitMQ
 */
@Service
@Component
public class SenderServiceImpl implements SenderService{
    private final RabbitTemplate rabbitTemplate;

    public SenderServiceImpl(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void send(String rabbitMQName, Answer answer) {
        rabbitTemplate.convertAndSend(rabbitMQName, answer);
    }

}
