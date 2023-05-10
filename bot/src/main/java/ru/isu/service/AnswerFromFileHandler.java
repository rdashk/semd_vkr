package ru.isu.service;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

public interface AnswerFromFileHandler {
    void getAnswerFromRabbitMQ(SendMessage sendMessage);
}
