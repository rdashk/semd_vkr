package ru.isu.service;

import org.jvnet.hk2.annotations.Service;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.isu.controller.FileController;
import ru.isu.model.db.Semd;
import ru.isu.repository.SemdRepository;

import static ru.isu.model.RabbitQueue.DOC_MESSAGE_UPDATE;
import static ru.isu.model.RabbitQueue.TEXT_MESSAGE_UPDATE;

@Service
@Component
public class RecipientServiceImpl implements RecipientService {

    private final SenderService sender;
    private final FileController fileController;
    @Autowired
    private SemdRepository semdRepository;


    public RecipientServiceImpl(SenderService sender, FileController fileController) {
        this.sender = sender;
        this.fileController = fileController;
    }

    @Override
    @RabbitListener(queues = TEXT_MESSAGE_UPDATE)
    public void getTextMessage(Update update) {
        //System.out.println("RecipientServiceImpl:get text message");

        var message = update.getMessage();
        var sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId().toString());
        String command = update.getMessage().getText();

        if (command.equals("/listSEMD")) {
            semdRepository.save(new Semd(86L, "Test1"));

            sendMessage.setText(fileController.getListSemd(semdRepository.findAll()));
            sender.sendTextMessage(sendMessage);
        } else {
            sendMessage.setText(fileController.getText(update));
            if (command.contains("/checkXML")) {
                sender.sendValidMessage(sendMessage);
            }else {
                sender.sendTextMessage(sendMessage);
            }
        }
    }

    @Override
    @RabbitListener(queues = DOC_MESSAGE_UPDATE)
    public void getDocMessage(Update update) {
        //System.out.println("RecipientServiceImpl:get document");

        var message = update.getMessage();
        var sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.setText(fileController.getDocument(update));
        sender.sendTextMessage(sendMessage);
    }
}
