package com.github.delegacy.youngbot.server.message.handler;

import static java.util.regex.Pattern.CASE_INSENSITIVE;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import com.github.delegacy.youngbot.server.RequestContext;

import reactor.core.publisher.Flux;

@Component
class PingMessageHandler implements MessageHandler {
    private static final Pattern PATTERN = Pattern.compile("^/?ping$", CASE_INSENSITIVE);

    private static final String PONG = "PONG";

    @Override
    public Pattern pattern() {
        return PATTERN;
    }

    @Override
    public Flux<String> process(RequestContext ctx, String text, Matcher matcher) {
        return Flux.just(PONG);
    }
}
