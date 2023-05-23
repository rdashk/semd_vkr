package ru.isu.service;

import ru.isu.model.Answer;

public interface AnswerFromFileHandler {
    void getAnswerFromRabbitMQ(Answer answer);
    void getValidFromRabbitMQ(Answer answer);
    void toPageFromRabbitMQ(Answer answer);
}
