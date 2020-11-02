package com.github.delegacy.youngbot.event.message;

import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static java.util.regex.Pattern.DOTALL;

import java.util.regex.MatchResult;
import java.util.regex.Pattern;

import com.github.delegacy.youngbot.event.EventProcessorContext;
import com.github.delegacy.youngbot.event.EventResponse;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * TBW.
 */
public class EchoProcessor extends AbstractMessageEventProcessor {
    private static final Pattern PATTERN = Pattern.compile("^/?echo\\s+(.+)$",
                                                           CASE_INSENSITIVE | DOTALL);

    private static final String ATTR_KEY = "MATCH_RESULT";

    @Override
    protected Mono<Boolean> shouldProcess(EventProcessorContext ctx, MessageEvent event) {
        return Mono.just(PATTERN.matcher(event.text()))
                   .map(matcher -> {
                       final boolean matched = matcher.matches();
                       ctx.attrs().put(ATTR_KEY, matcher.toMatchResult());
                       return matched;
                   });
    }

    @Override
    protected Flux<EventResponse> process(EventProcessorContext ctx, MessageEvent event) {
        return Mono.just((MatchResult) ctx.attrs().get(ATTR_KEY))
                   .map(matchResult -> EventResponse.of(matchResult.group(1)))
                   .flux();
    }
}
