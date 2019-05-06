package com.github.delegacy.youngbot.server.message.handler;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class MessageHandlerManager {
    private List<MessageHandler> handlers = Collections.emptyList();

    private final ApplicationContext applicationContext;

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
