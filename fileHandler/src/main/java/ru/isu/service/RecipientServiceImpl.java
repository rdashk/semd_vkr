package ru.isu.service;

import org.jvnet.hk2.annotations.Service;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.isu.controller.FileController;
import ru.isu.model.Answer;
import ru.isu.model.DocType;
import ru.isu.model.db.FileSemd;
import ru.isu.model.db.Semd;
import ru.isu.model.enums.Type;
import ru.isu.repository.FileSemdRepository;
import ru.isu.repository.SemdRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static ru.isu.model.RabbitQueue.*;

@Service
@Component
public class RecipientServiceImpl implements RecipientService {

    final String DESCR_GET_ZIP = "Архив СЭМД успешно загружен!";
    final String DESCR_ADD_ZIP = "\nЗагрузите архив СЭМД (имя архива = <b>КОД_СЭМД.zip</b>). " +
            "В архиве обязательно наличие:" +
            "1) шаблов(<b>xsd</b>)" +
            "2) текстового документа с названием СЭМД." +
            "\nДля проверки на соответствие схематрону - наличие файла(<b>sch</b>).";
    final String DESCR_SEMD = "CЭМД не выбран. \nВыбор СЭМД осуществляется автоматически " +
            "при загрузке вашего xml-документа.\nДля просмотра списка доступных СЭМД команда /listSEMD ";


    private final SenderService sender;
    private final FileController fileController;
    @Autowired
    private SemdRepository semdRepository;
    @Autowired
    private FileSemdRepository fileSemdRepository;


    public RecipientServiceImpl(SenderService sender, FileController fileController) {
        this.sender = sender;
        this.fileController = fileController;
    }

    @Override
    @RabbitListener(queues = TEXT_MESSAGE)
    public void getTextMessage(Answer answer) {
        //System.out.println("RecipientServiceImpl:get text message");

        var chatId = answer.getChatId();
        String textToSend = "";
        String command = answer.getMessage();

        if (command.equals("/listSEMD")) {
            textToSend = fileController.getListSemd(semdRepository.findAll());
            if (textToSend.isEmpty()) {
                textToSend = "Список СЭМД пустой!";
            } else {
                textToSend = "<b>Список доступных СЭМД</b>\n\n"+ textToSend;
            }
        } else if (command.equals("/listFiles")) {
            if (fileController.getSemdCode().isEmpty()) {
                textToSend = DESCR_SEMD;
            } else {
                textToSend = fileController.getListFiles(
                        fileSemdRepository.getFilesSemdByCode(Long.parseLong(fileController.getSemdCode())));
            }
        } else if (command.equals("/currentSEMD")) {
            if (fileController.getSemdCode().isEmpty()) {
                textToSend = DESCR_SEMD;
            } else {
                Semd semd = semdRepository.getSemdByCode(Long.valueOf(fileController.getSemdCode()));
                textToSend = "\nТекущий СЭМД =" + semd.getName() + " (код " + semd.getCode() + ").";

            }
        } else {
            textToSend = fileController.getText(command);
        }

        // choose type massage
        if (command.contains("/checkXML")) {
            sender.send(VALID_MESSAGE, new Answer(chatId, textToSend));
        } else {
            sender.send(ANSWER_MESSAGE, new Answer(chatId, textToSend));
        }
    }

    @Override
    @RabbitListener(queues = DOC_MESSAGE)
    public void getDocMessage(Update update) {
        //System.out.println("RecipientServiceImpl:get document");

        var message = update.getMessage();
        var sendMessage = new SendMessage();
        var chatId = message.getChatId().toString();
        sendMessage.setChatId(chatId);
        String textToSend = "";
        Type type = fileController.getDocType(update.getMessage().getDocument().getMimeType(),
                update.getMessage().getText());
        switch (type) {
            case ZIP -> {
                Document doc = update.getMessage().getDocument();
                Semd semd = fileController.getZip(
                        new DocType(doc.getFileName().substring(0, doc.getFileName().indexOf(".zip")),
                                update.getMessage().getText(),
                                type));
                if (semd.getName().isEmpty()) {
                    textToSend = "Отсутствует файл с названием СЭМД!" + DESCR_ADD_ZIP;
                } else if (semd.equals(new Semd())) {
                    textToSend = "Произошла ошибка! Проверьте содержимое архива " +
                            "и загрузите его еще раз!" + DESCR_ADD_ZIP;
                } else {
                    textToSend = DESCR_GET_ZIP;
                    semdRepository.save(semd);

                    // save all files from folder to db
                    // TODO: builder?
                    for (String filePath : fileController.getFilesFromZip()) {
                        try {
                            byte[] bytes = Files.readAllBytes(Paths.get(filePath));
                            fileSemdRepository.save(new FileSemd(semd.getCode(), filePath, bytes));
                        } catch (IOException e) {
                            System.out.println(e.getMessage());
                        }
                    }
                }
            }
            case XML -> {
                textToSend = fileController.getXml(update.getMessage().getChatId().toString(), update.getMessage().getText());
            }
            case SCH -> {
                textToSend = fileController.getSch(update.getMessage().getChatId().toString(), update.getMessage().getText());
            }
            default -> textToSend = "Ошибка!";
        }

        sender.send(ANSWER_MESSAGE, new Answer(chatId, textToSend));
    }

    @Override
    @RabbitListener(queues = WEB_MESSAGE)
    public void getWebMessage(Answer answer) {
        String textToSend = "";
        if (answer.getMessage().equals("/listSEMD")) {
            textToSend = fileController.getListSemd(semdRepository.findAll());
        }
        sender.send(WEB_ANSWER_MESSAGE, new Answer(answer.getChatId(), textToSend));
    }

}
