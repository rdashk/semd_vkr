package ru.isu.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
public class BotConfig {
    @Value("${bot.username}")
    private String botName;
    @Value("${bot.token}")
    private String botToken;
    @Value("${service.file_info.uri}")
    private String fileInfoUri;
    @Value("${service.file_storage.uri}")
    private String fileStorageUri;

    /*@Bean
    TelegramFileService getFileService() {
        return new TelegramFileServiceImpl();
    }

    @Bean
    SenderToRabbitMQ getSenderToRabbitMQ(RabbitTemplate rabbitTemplate) {
        return new SenderToRabbitMQImpl(rabbitTemplate);
    }*/
}
