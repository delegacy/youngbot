package com.github.delegacy.youngbot.line;

import static java.util.Objects.requireNonNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.delegacy.youngbot.event.EventResponse;
import com.github.delegacy.youngbot.event.EventService;

import com.linecorp.bot.model.event.CallbackRequest;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.MessageContent;
import com.linecorp.bot.model.event.message.TextMessageContent;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * TBW.
 */
public class LineService {
    private static final Logger logger = LoggerFactory.getLogger(LineService.class);

    private final EventService eventService;

    private final LineClient lineClient;

    /**
     * TBW.
     */
    public LineService(EventService eventService, LineClient lineClient) {
        this.eventService = requireNonNull(eventService, "eventService");
        this.lineClient = requireNonNull(lineClient, "lineClient");
    }

    /**
     * TBW.
     */
    public Mono<Void> handleCallback(CallbackRequest callback) {
        return toEventFlux(callback)
                .flatMap(this::processEvent)
                .onErrorResume(t -> {
                    logger.warn("Failed to process an event of callback<{}> and will resume the next",
                                callback, t);
                    return Mono.empty();
                })
                .then();
    }

    private static Flux<LineMessageEvent> toEventFlux(CallbackRequest callback) {
        return Flux.fromIterable(callback.getEvents())
                   .flatMap(event -> {
                       if (!(event instanceof MessageEvent)) {
                           return Mono.empty();
                       }

                       @SuppressWarnings("rawtypes")
                       final MessageEvent messageEvent = (MessageEvent) event;
                       final MessageContent messageContent = messageEvent.getMessage();
                       if (!(messageContent instanceof TextMessageContent)) {
                           return Mono.empty();
                       }

                       final TextMessageContent textMessageContent = (TextMessageContent) messageContent;
                       final String text = textMessageContent.getText();
                       final String channel = messageEvent.getSource().getSenderId();
                       logger.debug("Received text<{}> from channel<{}>", text, channel);

                       return Mono.just(LineMessageEvent.of(channel, text, messageEvent.getReplyToken()));
                   });
    }

    private Mono<Void> processEvent(LineEvent event) {
        final var flux = eventService.process(event);

        if (!(event instanceof LineReplyableEvent)) {
            return flux.then();
        }

        final var cast = (LineReplyableEvent) event;
        return flux.take(5)
                   .map(EventResponse::text)
                   .collectList()
                   .flatMap(list -> lineClient.replyMessage(cast.replyToken(), list))
                   .then();
    }
}
