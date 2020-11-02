package com.github.delegacy.youngbot.event.message;

import static java.util.regex.Pattern.CASE_INSENSITIVE;

import java.util.regex.Pattern;

import com.github.delegacy.youngbot.event.EventProcessorContext;
import com.github.delegacy.youngbot.event.EventResponse;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * TBW.
 */
public class PingProcessor extends AbstractMessageEventProcessor {
    private static final Pattern PATTERN = Pattern.compile("^/?ping$", CASE_INSENSITIVE);

    private static final String PONG = "PONG";

    @Override
    protected Mono<Boolean> shouldProcess(EventProcessorContext ctx, MessageEvent event) {
        return Mono.just(PATTERN.matcher(event.text()).matches());
    }

    @Override
    protected Flux<EventResponse> process(EventProcessorContext ctx, MessageEvent event) {
        return Flux.just(EventResponse.of(PONG));
    }
}
