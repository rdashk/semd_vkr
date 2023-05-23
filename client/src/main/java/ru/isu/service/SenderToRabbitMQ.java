package ru.isu.service;

import org.telegram.telegrambots.meta.api.objects.Update;
import ru.isu.model.Answer;

public interface SenderToRabbitMQ {
    /**
     * transmit message to RabbitMQ
     *
     * @param rabbitMQ - queue name
     * @param answer   - update from chat
     */
    void send(String rabbitMQ, Answer answer);

    void sendUpdate(String rabbitMQ, Update update);
}
