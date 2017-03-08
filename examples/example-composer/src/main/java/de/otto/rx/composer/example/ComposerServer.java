package de.otto.rx.composer.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan("de.otto.rx.composer")
@SpringBootApplication
public class ComposerServer {
    public static void main(String[] args) {
        SpringApplication.run(ComposerServer.class, args);
    }
}
