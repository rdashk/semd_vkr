package ru.isu.service;

import ru.isu.model.Answer;

/**
 * Send result working with files
 */
public interface SenderService {
    void send(String rabbitMQName, Answer answer);

}
