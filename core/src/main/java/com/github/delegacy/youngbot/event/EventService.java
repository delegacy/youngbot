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
                   .map(p -> new EventProcessorContext(p, event))
                   .filterWhen(ctx -> ctx.processor().shouldProcess(ctx, event))
                   .concatMap(ctx -> ctx.processor().process(ctx, event));
    }
}
