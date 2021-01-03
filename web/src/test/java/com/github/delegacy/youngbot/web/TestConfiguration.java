package com.github.delegacy.youngbot.web;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.delegacy.youngbot.event.EventService;
import com.github.delegacy.youngbot.internal.testing.AbstractTestConfiguration;

@Configuration
public class TestConfiguration extends AbstractTestConfiguration {
    @Bean
    public MessageController messageController(EventService eventService) {
        return new MessageController(eventService);
    }
}
