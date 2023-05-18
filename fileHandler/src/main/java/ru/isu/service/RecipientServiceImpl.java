package ru.isu.service;

import org.jvnet.hk2.annotations.Service;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.isu.controller.FileController;
import ru.isu.model.DocType;
import ru.isu.model.db.FileSemd;
import ru.isu.model.db.Semd;
import ru.isu.model.enums.Type;
import ru.isu.repository.FileSemdRepository;
import ru.isu.repository.SemdRepository;

import static ru.isu.model.RabbitQueue.DOC_MESSAGE_UPDATE;
import static ru.isu.model.RabbitQueue.TEXT_MESSAGE_UPDATE;

@Service
@Component
public class RecipientServiceImpl implements RecipientService {

    final String DESCR_GET_ZIP = "Архив СЭМД успешно загружен!";
    final String DESCR_ADD_ZIP = "\nЗагрузите архив СЭМД (имя архива = <b>КОД_СЭМД.zip</b>).В архиве обязательно наличие:" +
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
    @RabbitListener(queues = TEXT_MESSAGE_UPDATE)
    public void getTextMessage(Update update) {
        //System.out.println("RecipientServiceImpl:get text message");

        var message = update.getMessage();
        var sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId().toString());
        String textToSend = "";
        String command = update.getMessage().getText();

        if (command.equals("/listSEMD")) {
            textToSend = fileController.getListSemd(semdRepository.findAll());
        } else if (command.equals("/listFiles")) {
            if (fileController.getSemdCode().isEmpty()) {
                textToSend = DESCR_SEMD;
            } else {
                textToSend = fileController.getListFiles(fileSemdRepository.getFilesSemdByCode(Long.parseLong(fileController.getSemdCode())));
            }
        } else if (command.equals("/currentSEMD")) {
            if (fileController.getSemdCode().isEmpty()) {
                textToSend = DESCR_SEMD;
            } else {
                var semdTitle = semdRepository.findById(Long.valueOf(fileController.getSemdCode())).get();
                textToSend = "\nТекущий СЭМД =" + semdTitle.getName() +" (код "+semdTitle.getCode() + ").";

            }
        } else {
            textToSend = fileController.getText(update);
        }
        sendMessage.setText(textToSend);

        // choose type massage
        if (command.contains("/checkXML")) {
            sender.sendValidMessage(sendMessage);
        }else {
            sender.sendTextMessage(sendMessage);
        }
    }

    @Override
    @RabbitListener(queues = DOC_MESSAGE_UPDATE)
    public void getDocMessage(Update update) {
        //System.out.println("RecipientServiceImpl:get document");

        var message = update.getMessage();
        var sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId().toString());

        Type type = fileController.getDocType(update.getMessage());
        switch (type) {
            case ZIP -> {
                Document doc = update.getMessage().getDocument();
                Semd semd = fileController.getZip(
                        new DocType(doc.getFileName().substring(0, doc.getFileName().indexOf(".zip")),
                                update.getMessage().getText(),
                                type));
                if (semd.getName().isEmpty()) {
                    sendMessage.setText("Отсутствует файл с названием СЭМД!"+DESCR_ADD_ZIP);
                } else if (semd.equals(new Semd())) {
                    sendMessage.setText("Произошла ошибка! Проверьте содержимое архива и загрузите его еще раз!"+DESCR_ADD_ZIP);
                } else {
                    sendMessage.setText(DESCR_GET_ZIP);
                    semdRepository.save(semd);

                    // save all files from folder to db
                    // TODO: builder?
                    for (String s: fileController.getFilesFromZip()) {
                        fileSemdRepository.save(new FileSemd(semd.getCode(), s));
                    }
                }
            }
            case XML -> {
                sendMessage.setText(fileController.getXml(update.getMessage()));
            }
            case SCH -> {
                sendMessage.setText(fileController.getSch(update.getMessage()));
            }
            default -> sendMessage.setText("Ошибка!");
        }

        sender.sendTextMessage(sendMessage);
    }
}
