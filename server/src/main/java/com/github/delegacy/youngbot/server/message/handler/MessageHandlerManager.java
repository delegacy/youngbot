package com.github.delegacy.youngbot.server.message.handler;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class MessageHandlerManager {
    private List<MessageHandler> handlers = Collections.emptyList();

    private final ApplicationContext applicationContext;

    public MessageHandlerManager(ApplicationContext applicationContext) {
        this.applicationContext = requireNonNull(applicationContext, "applicationContext");
    }

    @PostConstruct
    public void init() {
        handlers = applicationContext.getBeansOfType(MessageHandler.class)
                                     .values().stream()
                                     .collect(Collectors.toUnmodifiableList());
    }

    public Collection<MessageHandler> handlers() {
        return handlers;
    }
}
