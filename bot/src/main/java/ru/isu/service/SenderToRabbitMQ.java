package ru.isu.service;

import org.telegram.telegrambots.meta.api.objects.Update;

public interface SenderToRabbitMQ {
    /**
     * transmit message to RabbitMQ
     * @param rabbitMQ - queue name
     * @param update - update from chat
     */
    void send(String rabbitMQ, Update update);
}
