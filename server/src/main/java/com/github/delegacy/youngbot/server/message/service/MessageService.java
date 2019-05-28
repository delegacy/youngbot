package com.github.delegacy.youngbot.server.message.service;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.github.delegacy.youngbot.server.ReactorContextFilter;
import com.github.delegacy.youngbot.server.RequestContext;
import com.github.delegacy.youngbot.server.message.handler.MessageHandler;
import com.github.delegacy.youngbot.server.message.handler.MessageHandlerManager;
import com.github.delegacy.youngbot.server.platform.PlatformServiceManager;

import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;
import reactor.util.context.Context;

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

    public void process(RequestContext ctx, String message) {
        final Collection<MessageHandler> handlers = messageHandlerManager.handlers();
        final List<Flux<String>> fluxes = new ArrayList<>(handlers.size());
        for (MessageHandler handler : handlers) {
            final Matcher matcher = handler.pattern().matcher(message);

            if (!matcher.matches()) {
                continue;
            }

            fluxes.add(handler.process(ctx, message, matcher));
        }

        Flux.concat(fluxes)
            .flatMap(response -> platformServiceManager.get(ctx.platform())
                                                       .replyMessage(ctx.replyTo(), response))
            .doOnNext(ignored -> logger.info("Succeeded to process;message<{}>", message))
            .doOnError(t -> logger.warn("Failed to process;message<{}>", message, t))
            .subscribeOn(Schedulers.elastic())
            .subscriberContext((Context) ctx.exchange()
                                            .getRequiredAttribute(ReactorContextFilter.REACTOR_CONTEXT_KEY))
            .subscribe();
    }
}
