package com.github.delegacy.youngbot.line;

import static org.mockito.Mockito.mock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.delegacy.youngbot.event.EventService;
import com.github.delegacy.youngbot.internal.testing.AbstractTestConfiguration;

import com.linecorp.bot.parser.LineSignatureValidator;

@Configuration
public class TestConfiguration extends AbstractTestConfiguration {
    @Bean
    public LineSignatureValidator lineSignatureValidator() {
        return mock(LineSignatureValidator.class);
    }

    @Bean
    public LineClient lineClient() {
        return mock(LineClient.class);
    }

    @Bean
    public LineService lineService(EventService eventService, LineClient lineClient) {
        return new LineService(eventService, lineClient);
    }

    @Bean
    public LineController lineController(LineService lineService,
                                         LineSignatureValidator lineSignatureValidator) {
        return new LineController(lineService, lineSignatureValidator);
    }
}
