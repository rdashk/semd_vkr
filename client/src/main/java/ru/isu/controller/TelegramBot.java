package ru.isu.controller;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.isu.config.BotConfig;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


@Component
public class TelegramBot extends TelegramLongPollingBot {

    final BotConfig config;
    private BotController botController;
    public TelegramBot(BotConfig config, BotController botController) {
        this.config = config;
        this.botController = botController;
    }
    //@Value("${bot.username}") String botName;
    //@Value("${bot.token}") String botToken;

    /**
     * Send message to controller
     */
    @PostConstruct
    public void init() {
        botController.createBot(this);
    }
    @Override
    public String getBotUsername() {
        return config.getBotName();
        //return botName;
    }

    @Override
    public String getBotToken() {
        return config.getBotToken();
        //return botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        botController.validData(update);
    }

    public void sendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);
        // for appearance editing message
        message.enableHtml(true);
        executeMessage(message);
    }

    public void sendFile(long chatId, List<String> files) {
        SendDocument doc = new SendDocument();
        for (String filePath:files) {
            doc = new SendDocument();
            doc.setChatId(chatId);
            doc.setDocument(new InputFile(new File(filePath)));
            executeFileMessage(doc);
        }
    }

    private void executeFileMessage(SendDocument message){
        try {
            execute(message);
        } catch (TelegramApiException e) {
            System.out.println(e.getMessage());
        }
    }

    public void sendMessageWithKeyBoard(long chatId, String textToSend, int listSize) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);

        message.setReplyMarkup(addKeyBoard(listSize));
        // for appearance editing message
        message.enableHtml(true);
        executeMessage(message);
    }

    private void executeMessage(SendMessage message){
        try {
            execute(message);
        } catch (TelegramApiException e) {
            System.out.println(e.getMessage());
        }
    }

    private ReplyKeyboardMarkup addKeyBoard(int listSize) {
        ReplyKeyboardMarkup keyMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyList = new ArrayList<>();
        KeyboardRow rowButtons = new KeyboardRow();

        int i, line = 5;
        for (i=1;i<listSize+1;i++){
            rowButtons.add(String.valueOf(i));
            if (i == line) {
                keyList.add(rowButtons);
                rowButtons = new KeyboardRow();
                line += 5;
            }
        }
        if (i%5 != 0) keyList.add(rowButtons);
        keyMarkup.setKeyboard(keyList);
        return keyMarkup;
    }
}
