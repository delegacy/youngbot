package com.github.delegacy.youngbot.internal.testing;

import java.util.Set;

import org.springframework.context.annotation.Bean;

import com.github.delegacy.youngbot.event.EventProcessor;
import com.github.delegacy.youngbot.event.EventService;
import com.github.delegacy.youngbot.event.message.PingProcessor;

public abstract class AbstractTestConfiguration {
    @Bean
    public PingProcessor pingProcessor() {
        return new PingProcessor();
    }

    @Bean
    public EventService eventService(Set<EventProcessor> processors) {
        return new EventService(processors);
    }
}
