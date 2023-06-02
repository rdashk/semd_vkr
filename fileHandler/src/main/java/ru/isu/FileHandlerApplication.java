package ru.isu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
/*@EnableJpaRepositories("ru.isu.*")
@ComponentScan(basePackages={"ru.isu.*"})*/
public class FileHandlerApplication {

    public static void main(String[] args) {
        SpringApplication.run(FileHandlerApplication.class, args);
    }

}
