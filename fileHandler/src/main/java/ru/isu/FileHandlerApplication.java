package ru.isu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories("ru.isu.*")
@ComponentScan(basePackages = { "ru.isu.*" })
@EntityScan("ru.isu.*")
public class FileHandlerApplication {

    public static void main(String[] args) {
        SpringApplication.run(FileHandlerApplication.class, args);
    }

}
