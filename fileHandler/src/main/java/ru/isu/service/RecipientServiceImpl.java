package ru.isu.service;

import org.jvnet.hk2.annotations.Service;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.isu.controller.FileController;

import static ru.isu.model.RabbitQueue.DOC_MESSAGE_UPDATE;
import static ru.isu.model.RabbitQueue.TEXT_MESSAGE_UPDATE;

@Service
public class RecipientServiceImpl implements RecipientService {

    private final SenderService sender;
    private final FileController fileController;

    public RecipientServiceImpl(SenderService sender, FileController fileController) {
        this.sender = sender;
        this.fileController = fileController;
    }

    @Override
    @RabbitListener(queues = TEXT_MESSAGE_UPDATE)
    public void getTextMessage(Update update) {
        System.out.println("RecipientServiceImpl:get text message");

        var message = update.getMessage();
        var sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.setText(fileController.getText(update));
        sender.sendTextMessage(sendMessage);
    }

    @Override
    @RabbitListener(queues = DOC_MESSAGE_UPDATE)
    public void getDocMessage(Update update) {
        System.out.println("RecipientServiceImpl:get document");

        var message = update.getMessage();
        var sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.setText(fileController.getDocument(update));
        sender.sendTextMessage(sendMessage);
    }
}
