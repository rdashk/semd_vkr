package ru.isu.controller;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.isu.model.Answer;
import ru.isu.model.db.SystemUser;
import ru.isu.repository.UserRepository;
import ru.isu.service.SenderToRabbitMQ;
import ru.isu.service.TelegramFileService;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static ru.isu.model.RabbitQueue.DOC_MESSAGE;
import static ru.isu.model.RabbitQueue.TEXT_MESSAGE;

/**
 * All action with telegram bot
 */
@Component
public class BotController{

    private TelegramBot telegramBot;
    final String DESCR_BOT = "\n\nЭто бот для проверки СЭМД на соответствие его пакету спецификации.\n\n<b>Команды:</b>\n\n" +
            "/start - начало работы с ботом\n" +
            "/help - список команд\n" +
            "/listSEMD - список пакетов спецификации СЭМД\n" +
            "/listFiles - список файлов текущего пакета спецификации\n" +
            "/currentSEMD - текущий пакет спецификации\n" +
            "/checkXML - проверка СЭМД\n" +
            "/checkXML_body - проверка содержимого <b>тела</b> СЭМД";

    private final TelegramFileService fileService;
    private final SenderToRabbitMQ sender;

    @Autowired
    private UserRepository userRepository;

    public BotController(TelegramFileService fileService, SenderToRabbitMQ sender) {
        this.fileService = fileService;
        this.sender = sender;
    }

    public void createBot(TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
    }

    /**
     * Checking message from user
     * @param update Update
     */
    public void validData(Update update) {
        if (update == null) {
            createSendMessage(null, "Повторите отправку!");
            return;
        }

        if (update.hasMessage() ){
            // message type
            messageType(update);
        } else if (update.hasCallbackQuery()) { // if sent button id
            String nameButton = update.getCallbackQuery().getData();
            //int messageId = update.getCallbackQuery().getMessage().getMessageId();
            long chatId = update.getCallbackQuery().getMessage().getChatId();

            EditMessageText newMess = new EditMessageText();
            newMess.setChatId(chatId);
            // id кнопки
            createSendMessage(update, nameButton);
        } else {
            createSendMessage(update, "Пустое сообщение!");
        }
    }

    /**
     * Get message type (text, document or unsupported type
     * @param update This object represents an incoming update.
     *               update1 = update2, if update1_id = update2_id.
     */
    private void messageType(Update update) {
        var message = update.getMessage();

        if (message.hasText()) {
            textMessage(update);
        } else if (message.hasDocument()) {
            getFileType(update);
        } else {
            createSendMessage(update, "Неподдерживаемый тип файла!");
        }
    }

    /**
     * Actions if message type is text
     * @param update Update
     */
    private void textMessage(Update update) {
        var message = update.getMessage();
        String messageText = message.getText();

        switch (messageText) {
            case "/help" -> telegramBot.sendMessage(message.getChatId(),
                    DESCR_BOT);
            case "/start" -> {
                telegramBot.sendMessage(message.getChatId(),
                        "Здравствуйте, " + message.getChat().getFirstName() + "!" + DESCR_BOT);
                userRepository.save(new SystemUser(new Date().toString(), message.getChat().getUserName(), message.getChatId().toString()));
            }
            default -> {
                createSendMessage(update, "Обрабатываю команду...");
                sender.send(TEXT_MESSAGE, new Answer(message.getChatId().toString(), messageText));
            }
        }

    }

    /**
     * Getting file type
     * @param update This object represents an incoming update.
     *               update1 = update2, if update1_id = update2_id.
     */
    private void getFileType(Update update) {
        createSendMessage(update, "Проверяю тип файла...");

        String telegramFilePath = fileService.checkTypeDoc(update);
        if (telegramFilePath.isEmpty()) {
            createSendMessage(update, "Неподдерживаемый тип файла!");
        }
        else if (telegramFilePath.equals("wrong_name_zip")) {
            createSendMessage(update, "Название пакета спецификации должно быть равно коду СЭМД!");
        }
        else {
            // send file types: XSD, SCH, XML, ZIP
            update.getMessage().setText(telegramFilePath);
            // TODO: send other object = not update
            sender.sendUpdate(DOC_MESSAGE, update);
        }
    }

    public void createSendMessage(Update update, String text) {
        telegramBot.sendMessage(update.getMessage().getChatId(), text);
    }

    public void createAnswerMessage(SendMessage message) {
        telegramBot.sendMessage(Long.parseLong(message.getChatId()), message.getText());
    }

    public void createValidMessage(SendMessage message) {

        telegramBot.sendMessage(Long.parseLong(message.getChatId()), message.getText());
        // if found some errors
        List<String> pathFilesWithErrors = hasError(message.getChatId() +"/errors");
        //System.out.println(pathFilesWithErrors.toString());
        if (!pathFilesWithErrors.isEmpty()) {
            telegramBot.sendFile(Long.parseLong(message.getChatId()), pathFilesWithErrors);
        }
        deleteFolder(message.getChatId());
    }

    private void deleteFolder(String folderName) {
        if (new File(folderName).exists()) {
            try {
                FileUtils.forceDelete(new File(folderName));
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private List<String> hasError(String folder) {
        List<String> files = new ArrayList<>();
        if (new File(folder).exists()) {

            if (new File(folder+"/errors_shema.txt").exists()) {
                files.add(folder+"/errors_shema.txt");
            }
            if (new File(folder+"/errors_schematron.pdf").exists()) {
                files.add(folder+"/errors_schematron.pdf");
                files.add(folder+"/errors_schematron.txt");
            }
            if (new File(folder+"/errors_body.pdf").exists()) {
                files.add(folder+"/errors_body.pdf");
                files.add(folder+"/errors_body.txt");
            }
        }


        return files;
    }
}
