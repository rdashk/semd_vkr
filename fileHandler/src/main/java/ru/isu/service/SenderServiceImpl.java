package ru.isu.service;

import org.jvnet.hk2.annotations.Service;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import static ru.isu.model.RabbitQueue.ANSWER_MESSAGE;
import static ru.isu.model.RabbitQueue.VALID_MESSAGE;

/**
 * Install listener for every queue in RabbitMQ
 */
@Service
public class SenderServiceImpl implements SenderService{
    private final RabbitTemplate rabbitTemplate;

    public SenderServiceImpl(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void sendTextMessage(SendMessage sendMessage) {
        rabbitTemplate.convertAndSend(ANSWER_MESSAGE, sendMessage);
    }

    @Override
    public void sendValidMessage(SendMessage sendMessage) {
        rabbitTemplate.convertAndSend(VALID_MESSAGE, sendMessage);
    }
}
