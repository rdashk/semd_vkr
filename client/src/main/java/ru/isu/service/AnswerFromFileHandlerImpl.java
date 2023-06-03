package ru.isu.service;

import org.jvnet.hk2.annotations.Service;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.isu.controller.BotController;
import ru.isu.model.Answer;

import static ru.isu.model.RabbitQueue.ANSWER_MESSAGE;
import static ru.isu.model.RabbitQueue.VALID_MESSAGE;


@Service
@Component
public class AnswerFromFileHandlerImpl implements AnswerFromFileHandler{

    private final BotController botController;

    public AnswerFromFileHandlerImpl(BotController botController) {
        this.botController = botController;
    }

    @Override
    @RabbitListener(queues = ANSWER_MESSAGE)
    public void getAnswerFromRabbitMQ(Answer answer) {

        var sendMessage = new SendMessage();
        sendMessage.setChatId(answer.getChatId());
        sendMessage.setText(answer.getMessage());
        botController.createAnswerMessage(sendMessage);
    }

    @Override
    @RabbitListener(queues = VALID_MESSAGE)
    public void getValidFromRabbitMQ(Answer answer) {

        var sendMessage = new SendMessage();
        sendMessage.setChatId(answer.getChatId());
        sendMessage.setText(answer.getMessage());
        botController.createValidMessage(sendMessage);
    }
}
