package com.github.delegacy.youngbot.server.message.processor;

import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static java.util.regex.Pattern.DOTALL;

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
class EchoMessageProcessor implements MessageProcessor {
    private static final Pattern PATTERN = Pattern.compile("^/?echo\\s+(.+)$",
                                                           CASE_INSENSITIVE | DOTALL);

    @Override
    public Pattern pattern() {
        return PATTERN;
    }

    @Override
    public Flux<MessageResponse> process(MessageRequest request, Matcher matcher) {
        return Flux.just(MessageResponse.of(request, matcher.group(1)));
    }
}
