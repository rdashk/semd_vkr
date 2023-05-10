package ru.isu.service;

import ru.isu.model.DocType;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface TelegramFileService {

    DocType checkTypeDoc(Update telegramDoc);
    String processDoc(Update telegramMessage) throws Exception;

}
