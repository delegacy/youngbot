package com.github.delegacy.youngbot.line;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.delegacy.youngbot.message.MessageResponse;
import com.github.delegacy.youngbot.message.MessageService;

import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.event.CallbackRequest;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.MessageContent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.message.TextMessage;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

/**
 * TBW.
 */
public class LineService {
    private static final Logger logger = LoggerFactory.getLogger(LineService.class);

    private final LineMessagingClient lineMessagingClient;

    private final MessageService messageService;

    /**
     * TBW.
     */
    public LineService(LineMessagingClient lineMessagingClient, MessageService messageService) {
        this.lineMessagingClient = requireNonNull(lineMessagingClient, "lineMessagingClient");
        this.messageService = requireNonNull(messageService, "messageService");
    }

    /**
     * TBW.
     */
    public Flux<Void> handleCallback(CallbackRequest callback) {
        return toMessageRequestFlux(callback)
                .flatMap(this::handleMessage);
    }

    private static Flux<LineMessageRequest> toMessageRequestFlux(CallbackRequest req) {
        return Flux.fromIterable(req.getEvents())
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

                       return Mono.just(new LineMessageRequest(channel, text, messageEvent.getReplyToken()));
                   });
    }

    private Flux<Void> handleMessage(LineMessageRequest req) {
        return messageService.process(req)
                             .take(5)
                             .collectMultimap(MessageResponse::request, MessageResponse::text)
                             .flatMapMany(m -> Flux.fromIterable(m.entrySet())
                                                   .map(e -> Tuples.of((LineMessageRequest) e.getKey(),
                                                                       e.getValue())))
                             .flatMap(tuple -> replyMessage(tuple.getT1(), tuple.getT2()));
    }

    private Mono<Void> replyMessage(LineMessageRequest req, Collection<String> responses) {
        return Mono.fromFuture(
                lineMessagingClient.replyMessage(
                        new ReplyMessage(req.replyToken(), responses.stream()
                                                                     .map(TextMessage::new)
                                                                     .collect(Collectors.toList()))))
                   .doOnNext(res -> logger.debug("Replied to text<{}> in channel<{}>;res<{}>",
                                                 req.text(), req.channel(), res))
                   .doOnError(t -> logger.warn("Failed to reply to channel<{}> in chat<{}>",
                                               req.text(), req.channel(), t))
                   .onErrorResume(t -> Mono.empty())
                   .then();
    }
}
