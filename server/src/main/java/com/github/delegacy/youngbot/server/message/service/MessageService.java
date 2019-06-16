package com.github.delegacy.youngbot.server.message.service;

import static java.util.Objects.requireNonNull;

import java.util.regex.Matcher;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.github.delegacy.youngbot.server.message.MessageContext;
import com.github.delegacy.youngbot.server.TheVoid;
import com.github.delegacy.youngbot.server.message.handler.MessageHandlerManager;
import com.github.delegacy.youngbot.server.platform.PlatformServiceManager;

import reactor.core.publisher.Flux;

@Service
public class MessageService {
    private static final Logger logger = LoggerFactory.getLogger(MessageService.class);

    private final MessageHandlerManager messageHandlerManager;

    private final PlatformServiceManager platformServiceManager;

    public MessageService(MessageHandlerManager messageHandlerManager,
                          PlatformServiceManager platformServiceManager) {

        this.messageHandlerManager = requireNonNull(messageHandlerManager, "messageHandlerManager");
        this.platformServiceManager = requireNonNull(platformServiceManager, "platformServiceManager");
    }

    public Flux<TheVoid> process(MessageContext msgCtx) {
        final String text = msgCtx.text();

        return Flux.fromIterable(messageHandlerManager.handlers())
                   .concatMap(handler -> {
                       final Matcher matcher = handler.pattern().matcher(text);
                       if (!matcher.matches()) {
                           return Flux.empty();
                       }
                       return handler.handle(msgCtx, matcher);
                   })
                   .filter(StringUtils::isNotEmpty)
                   .concatMap(response -> platformServiceManager.get(msgCtx.platform())
                                                                .replyMessage(msgCtx, response))
                   .doOnNext(ignored -> logger.info("Processed text<{}>", text))
                   .doOnError(t -> logger.warn("Failed to process text<{}>", text, t));
    }
}
