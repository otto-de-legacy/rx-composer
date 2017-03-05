package de.otto.rx.composer.thymeleaf;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ThymeleafConfiguration {

    @Bean
    RxComposerDialect rxcDialect() {
        return new RxComposerDialect();
    }
}
