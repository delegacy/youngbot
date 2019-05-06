package com.github.delegacy.youngbot.server.message.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;

import org.springframework.stereotype.Service;

import com.github.delegacy.youngbot.server.RequestContext;
import com.github.delegacy.youngbot.server.message.handler.MessageHandler;
import com.github.delegacy.youngbot.server.message.handler.MessageHandlerManager;
import com.github.delegacy.youngbot.server.platform.PlatformServiceManager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

@Slf4j
@RequiredArgsConstructor
@Service
public class MessageService {
    private final MessageHandlerManager messageHandlerManager;

    private final PlatformServiceManager platformServiceManager;

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
            .subscribeOn(Schedulers.elastic())
            .doOnNext(ignored -> log.info("Succeeded to process;message<{}>", message))
            .doOnError(t -> log.warn("Failed to process;message<{}>", message, t))
            .subscribe();
    }
}
