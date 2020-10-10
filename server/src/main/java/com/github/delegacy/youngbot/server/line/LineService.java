package com.github.delegacy.youngbot.server.line;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.github.delegacy.youngbot.server.message.MessageContext;
import com.github.delegacy.youngbot.server.message.handler.MessageHandlerManager;

import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.event.CallbackRequest;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.MessageContent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.message.TextMessage;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

/**
 * TBW.
 */
@Service
class LineService {
    private static final Logger logger = LoggerFactory.getLogger(LineService.class);

    private final LineMessagingClient lineMessagingClient;

    private final MessageHandlerManager messageHandlerManager;

    /**
     * TBW.
     */
    LineService(LineMessagingClient lineMessagingClient, MessageHandlerManager messageHandlerManager) {
        this.messageHandlerManager = requireNonNull(messageHandlerManager, "messageHandlerManager");
        this.lineMessagingClient = requireNonNull(lineMessagingClient, "lineMessagingClient");
    }

    Flux<Void> handleCallback(CallbackRequest callback) {
        return toMessageContextFlux(callback)
                .flatMap(this::handleMessage);
    }

    private static Flux<MessageContext> toMessageContextFlux(CallbackRequest request) {
        return Flux.fromIterable(request.getEvents())
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
                       final String chat = messageEvent.getSource().getSenderId();
                       logger.debug("Received text<{}> from chat<{}>", text, chat);

                       return Mono.just(new LineMessageContext(text, messageEvent.getReplyToken(), chat));
                   });
    }

    private Flux<Void> handleMessage(MessageContext msgCtx) {
        return Flux.fromIterable(messageHandlerManager.handlers())
                   .concatMap(handler -> {
                       final Matcher matcher = handler.pattern().matcher(msgCtx.text());
                       if (!matcher.matches()) {
                           return Flux.empty();
                       }
                       return handler.handle(msgCtx, matcher);
                   })
                   .filter(resText -> !resText.isEmpty())
                   .take(5)
                   .map(resText -> Tuples.of(msgCtx, resText))
                   .collectMultimap(Tuple2::getT1, Tuple2::getT2)
                   .flatMapMany(m -> Flux.fromIterable(m.entrySet())
                                         .map(e -> Tuples.of(e.getKey(), e.getValue())))
                   .flatMap(tuple -> reply(tuple.getT1(), tuple.getT2()));
    }

    private Mono<Void> reply(MessageContext msgCtx, Collection<String> resTexts) {
        return Mono.fromFuture(
                lineMessagingClient.replyMessage(
                        new ReplyMessage(((LineMessageContext) msgCtx).replyToken(),
                                         resTexts.stream()
                                                 .map(TextMessage::new)
                                                 .collect(Collectors.toList()))))
                   .doOnNext(res -> logger.debug("Replied to text<{}> in chat<{}>;res<{}>",
                                                 msgCtx.text(), msgCtx.channelId(), res))
                   .doOnError(t -> logger.warn("Failed to reply to text<{}> in chat<{}>",
                                               msgCtx.text(), msgCtx.channelId(), t))
                   .onErrorResume(t -> Mono.empty())
                   .then();
    }
}
