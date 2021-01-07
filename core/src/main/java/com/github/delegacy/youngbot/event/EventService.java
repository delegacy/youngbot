package com.github.delegacy.youngbot.event;

import static java.util.Objects.requireNonNull;

import java.util.Set;

import reactor.core.publisher.Flux;

/**
 * TBW.
 */
public class EventService {
    private final Set<EventProcessor> processors;

    /**
     * TBW.
     */
    public EventService(Set<EventProcessor> processors) {
        this.processors = requireNonNull(processors, "processors");
    }

    /**
     * TBW.
     */
    public Flux<EventResponse> process(Event event) {
        return Flux.fromIterable(processors)
                   .concatMap(p -> p.process(event))
                   .filter(res -> !res.text().isEmpty())
                   .subscriberContext(ctx -> ctx.put(EventContext.class, new DefaultEventContext(event)));
    }
}
