package com.github.delegacy.youngbot.message.processor;

import static java.util.regex.Pattern.CASE_INSENSITIVE;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.delegacy.youngbot.message.MessageRequest;
import com.github.delegacy.youngbot.message.MessageResponse;

import reactor.core.publisher.Flux;

/**
 * TBW.
 */
public class PingMessageProcessor implements MessageProcessor {
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
