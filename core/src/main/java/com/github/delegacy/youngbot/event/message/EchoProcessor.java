package com.github.delegacy.youngbot.event.message;

import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static java.util.regex.Pattern.DOTALL;

import java.util.regex.MatchResult;
import java.util.regex.Pattern;

import com.github.delegacy.youngbot.event.AbstractEventProcessor;
import com.github.delegacy.youngbot.event.EventContext;
import com.github.delegacy.youngbot.event.EventResponse;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * TBW.
 */
public class EchoProcessor extends AbstractEventProcessor<MessageEvent> {
    private static final Pattern PATTERN = Pattern.compile("^/?echo\\s+(.+)$",
                                                           CASE_INSENSITIVE | DOTALL);

    @Override
    protected Mono<Boolean> shouldProcess0(MessageEvent event) {
        return Mono.just(PATTERN.matcher(event.text()))
                   .flatMap(m -> EventContext.current()
                                             .map(ctx -> {
                                                 final var matched = m.matches();
                                                 ctx.attrs().put(this, m.toMatchResult());
                                                 return matched;
                                             }));
    }

    @Override
    protected Flux<EventResponse> process0(MessageEvent event) {
        return EventContext.current()
                           .map(ctx -> {
                               final var matchResult = (MatchResult) ctx.attrs().get(this);
                               return EventResponse.of(matchResult.group(1));
                           })
                           .flux();
    }
}
