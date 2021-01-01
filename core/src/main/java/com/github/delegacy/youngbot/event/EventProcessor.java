package com.github.delegacy.youngbot.event;

import reactor.core.publisher.Flux;

/**
 * TBW.
 */
@SuppressWarnings("InterfaceMayBeAnnotatedFunctional")
public interface EventProcessor {
    /**
     * TBW.
     */
    Flux<EventResponse> process(Event event);
}
