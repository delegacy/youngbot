package com.github.delegacy.youngbot.server.message.processor;

import static java.util.regex.Pattern.CASE_INSENSITIVE;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import com.github.delegacy.youngbot.server.message.MessageRequest;
import com.github.delegacy.youngbot.server.message.MessageResponse;

import reactor.core.publisher.Flux;

/**
 * TBW.
 */
@Component
class PingMessageProcessor implements MessageProcessor {
    private static final Pattern PATTERN = Pattern.compile("^/?ping$", CASE_INSENSITIVE);

    private static final String PONG = "PONG";

    @Override
    public Pattern pattern() {
        return PATTERN;
    }

    @Override
    public Flux<MessageResponse> process(MessageRequest request, Matcher matcher) {
        return Flux.just(MessageResponse.of(request, PONG));
    }
}
