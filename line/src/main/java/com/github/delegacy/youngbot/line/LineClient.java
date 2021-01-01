package com.github.delegacy.youngbot.line;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TextMessage;

import reactor.core.publisher.Mono;

/**
 * TBW.
 */
public class LineClient {
    private static final Logger logger = LoggerFactory.getLogger(LineClient.class);

    private final LineMessagingClient client;

    /**
     * TBW.
     */
    public LineClient(LineMessagingClient client) {
        this.client = requireNonNull(client, "client");
    }

    /**
     * TBW.
     */
    public LineMessagingClient rawClient() {
        return client;
    }

    /**
     * TBW.
     */
    public Mono<Void> replyMessage(String replyToken, List<String> messages) {
        return Mono.just(messages.stream()
                                 .map(msg -> (Message) new TextMessage(msg))
                                 .collect(Collectors.toUnmodifiableList()))
                   .map(list -> new ReplyMessage(replyToken, list))
                   .flatMap(r -> Mono.fromFuture(client.replyMessage(r)))
                   .doOnNext(res -> logger.debug("Replied with replyToken<{}>;res<{}>", replyToken, res))
                   .doOnError(t -> logger.error("Failed to reply with replyToken<{}>", replyToken, t))
                   .then();
    }
}
