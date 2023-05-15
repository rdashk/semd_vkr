package ru.isu.service;

import ru.isu.controller.BotController;


public class AnswerFromFileHandlerImpl {//implements AnswerFromFileHandler{

    private final BotController botController;

    public AnswerFromFileHandlerImpl(BotController botController) {
        this.botController = botController;
    }

    /*@Override
    @RabbitListener(queues = ANSWER_MESSAGE)
    public void getAnswerFromRabbitMQ(SendMessage sendMessage) {

        botController.createAnswerMessage(sendMessage);
    }*/
}
