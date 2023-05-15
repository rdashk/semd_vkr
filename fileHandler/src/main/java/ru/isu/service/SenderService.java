package ru.isu.service;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

/**
 * Send result working with files
 */
public interface SenderService {
    void sendTextMessage(SendMessage sendMessage);

    void sendValidMessage(SendMessage sendMessage);

}
