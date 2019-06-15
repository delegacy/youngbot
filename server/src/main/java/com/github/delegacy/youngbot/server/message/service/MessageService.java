package com.github.delegacy.youngbot.server.message.service;

import static java.util.Objects.requireNonNull;

import java.util.regex.Matcher;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.github.delegacy.youngbot.server.RequestContext;
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

    public Flux<TheVoid> process(RequestContext ctx) {
        final String text = ctx.text();

        return Flux.fromIterable(messageHandlerManager.handlers())
                   .concatMap(handler -> {
                       final Matcher matcher = handler.pattern().matcher(text);
                       if (!matcher.matches()) {
                           return Flux.empty();
                       }
                       return handler.process(ctx, matcher);
                   })
                   .filter(StringUtils::isNotEmpty)
                   .concatMap(response -> platformServiceManager.get(ctx.platform())
                                                                .replyMessage(ctx.replyTo(), response))
                   .doOnNext(ignored -> logger.info("Succeeded to process;text<{}>", text))
                   .doOnError(t -> logger.warn("Failed to process;text<{}>", text, t));
    }
}
