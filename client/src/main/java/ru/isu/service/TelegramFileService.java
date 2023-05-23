package ru.isu.service;

import org.telegram.telegrambots.meta.api.objects.Update;

public interface TelegramFileService {

    String checkTypeDoc(Update telegramDoc);
    String processDoc(Update telegramMessage) throws Exception;

}
