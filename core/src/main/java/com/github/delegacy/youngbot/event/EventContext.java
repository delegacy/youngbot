package com.github.delegacy.youngbot.event;

import java.util.Map;

import reactor.core.publisher.Mono;

/**
 * TBW.
 */
public interface EventContext {
    /**
     * TBW.
     */
    static Mono<EventContext> current() {
        return Mono.deferWithContext(ctx -> Mono.just(ctx.get(EventContext.class)));
    }

    /**
     * TBW.
     */
    Event event();

    /**
     * TBW.
     */
    Map<Object, Object> attrs();
}
