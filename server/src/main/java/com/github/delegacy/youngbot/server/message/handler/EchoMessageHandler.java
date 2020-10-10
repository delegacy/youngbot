package com.github.delegacy.youngbot.server.message.handler;

import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static java.util.regex.Pattern.DOTALL;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import com.github.delegacy.youngbot.server.message.MessageContext;

import reactor.core.publisher.Flux;

/**
 * TBW.
 */
@Component
public class EchoMessageHandler implements MessageHandler {
    private static final Pattern PATTERN = Pattern.compile("^/?echo\\s+(.+)$",
                                                           CASE_INSENSITIVE | DOTALL);

    @Override
    public Pattern pattern() {
        return PATTERN;
    }

    @Override
    public Flux<String> handle(MessageContext msgCtx, Matcher matcher) {
        return Flux.just(matcher.group(1));
    }
}
