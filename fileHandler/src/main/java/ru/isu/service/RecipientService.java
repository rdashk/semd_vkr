package ru.isu.service;

import org.telegram.telegrambots.meta.api.objects.Update;

/**
 * Get message from RabbitMQ
 */
public interface RecipientService {
    void getTextMessage(Update update);
    void getDocMessage(Update update);
}
