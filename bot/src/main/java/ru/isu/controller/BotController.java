package ru.isu.controller;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.isu.model.DocType;
import ru.isu.service.AnswerFromFileHandler;
import ru.isu.service.SenderToRabbitMQ;
import ru.isu.service.TelegramFileService;

import static ru.isu.model.RabbitQueue.*;

/**
 * All action with telegram bot
 */
@Component
public class BotController implements AnswerFromFileHandler {

    private TelegramBot telegramBot;
    final String DESCR_BOT = "\n\nЭто бот для проверки XML-документа на соответствие СЭМД\n\n<b>Команды:</b>\n\n" +
            "/start - начало работы с ботом\n" +
            "/help - список команд\n" +
            "/listSEMD - список доступных СЭМД\n" +
            "/listFiles - список доступных файлов текущего СЭМД\n" +
            "/changeSEMD - смена СЭМД\n" +
            "/currentSEMD - текущий СЭМД\n" +
            "/checkXML - проверка xml-документа\n" +
            "/checkXML_body - проверки <b>тела</b> xml-документа\n";
    final String DESCR_ADD_XML = "\nЗагрузите файл в формате <b>xml</b>." +
            "\nПРОВЕРЯЙТЕ РАЗРЕШЕНИЕ ФАЙЛА ПЕРЕД ЗАГРУЗКОЙ!";
    final String DESCR_ADD_ZIP = "\nЗагрузите архив СЭМД (имя архива = <b>КОД_СЭМД.zip</b>) с шаблонами(<b>xsd</b>) и схематроном(<b>sch</b>).";
    final String DESCR_GET_XML = "Файл <b>xml</b> успешно загружен! ";
    final String DESCR_GET_SCH = "Файл <b>sch</b> успешно загружен!";
    final String DESCR_GET_ZIP = "Папка с файлами успешно загружена!";
    final String DESCR_CHECK = "\n\n<b>Команды:</b>\n\n" +
            "/checkXML - выполнение проверки xml-документа на соответствие шаблонам и схематрону\n" +
            "/checkXML_body - выполнение проверки <b>тела</b> xml-документа на соответствие схематрону";
    final String DESCR_ANS = "<b>Результат проверки</b>\n\n";
    final String DESCR_SEMD = "Выберете СЭМД или загрузите новый архив\n\n<b>Команды:</b>\n\n" +
            "/listSEMD - список доступных СЭМД\n" +
            "/addNewSEMD - добавление нового СЭМД";

    private final TelegramFileService fileService;
    private final SenderToRabbitMQ sender;

    public BotController(TelegramFileService fileService, SenderToRabbitMQ sender) {
        this.fileService = fileService;
        this.sender = sender;
    }

    public void createBot(TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
    }

    /**
     * Checking message from user
     * @param update
     */
    public void validData(Update update) {
        if (update == null) {
            createSendMessage(update, "Повторите отправку!");
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
            unsupportedTypeMessage(update);
        }
    }

    private void unsupportedTypeMessage(Update update) {
        createSendMessage(update, "Неподдерживаемый тип файла!");
    }

    /**
     * Actions if message type is text
     * @param update
     */
    private void textMessage(Update update) {
        var message = update.getMessage();
        String messageText = message.getText();

        switch (messageText) {
            case "/help": {
                telegramBot.sendMessage(message.getChatId(),
                        DESCR_BOT);
                break;
            }
            case "/start": {
                telegramBot.sendMessage(message.getChatId(),
                        "Здравствуйте, " + message.getChat().getFirstName() + "!" + DESCR_BOT);
                break;
            }

            /*case "/deleteFolder" -> {
                telegramBot.sendMessage(message.getChatId(), "Удаление папки...");
                try {
                    files.deleteFolder(message.getChatId().toString());
                    telegramBot.sendMessage(message.getChatId(), "Папка с файлами успешно удалена.");
                } catch (IOException e) {
                    telegramBot.sendMessage(message.getChatId(), "Невозможно удалить папку!");
                    throw new RuntimeException(e);
                }
            }*/
            case "/checkXML": {
                createSendMessage(update, "Проверяю файл на соответствие схеме и схематрону...");
            }

            case "/checkXML_body": {
                createSendMessage(update, "Проверяю тело файла на соответствие телу схематрона...");
            }

            default: {
                sender.send(TEXT_MESSAGE_UPDATE, update);
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

        DocType docType = fileService.checkTypeDoc(update);
        if (docType == null) unsupportedTypeMessage(update);
        else {
            // send file types: XSD, SCH, XML, ZIP
            update.getMessage().setText(docType.getFilePath());
            sender.send(DOC_MESSAGE_UPDATE, update);
        }
    }

    public void createSendMessage(Update update, String text) {
        telegramBot.sendMessage(update.getMessage().getChatId(), text);
    }

    public void createAnswerMessage(SendMessage message) {
        telegramBot.sendMessage(Long.parseLong(message.getChatId()), message.getText().toString());
    }

    @Override
    @RabbitListener(queues = ANSWER_MESSAGE)
    public void getAnswerFromRabbitMQ(SendMessage sendMessage) {
        System.out.println("get answer!");
        createAnswerMessage(sendMessage);
    }
}
