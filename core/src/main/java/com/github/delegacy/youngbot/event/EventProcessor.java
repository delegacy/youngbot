package com.github.delegacy.youngbot.event;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * TBW.
 */
public interface EventProcessor {
    /**
     * TBW.
     */
    Mono<Boolean> shouldProcess(EventProcessorContext ctx, Event event);

    /**
     * TBW.
     */
    Flux<EventResponse> process(EventProcessorContext ctx, Event event);
}
