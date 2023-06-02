package ru.isu.service;

import org.telegram.telegrambots.meta.api.objects.Update;
import ru.isu.model.Answer;

/**
 * Get message from RabbitMQ
 */
public interface RecipientService {
    void getTextMessage(Answer answer);
    void getDocMessage(Update update);
}
