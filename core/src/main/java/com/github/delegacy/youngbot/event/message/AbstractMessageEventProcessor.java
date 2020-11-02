package com.github.delegacy.youngbot.event.message;

import com.github.delegacy.youngbot.event.Event;
import com.github.delegacy.youngbot.event.EventProcessor;
import com.github.delegacy.youngbot.event.EventProcessorContext;
import com.github.delegacy.youngbot.event.EventResponse;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * TBW.
 */
public abstract class AbstractMessageEventProcessor implements EventProcessor {
    /**
     * TBW.
     */
    @Override
    public Mono<Boolean> shouldProcess(EventProcessorContext ctx, Event event) {
        return Mono.just(event)
                   .filter(evt -> evt instanceof MessageEvent)
                   .cast(MessageEvent.class)
                   .flatMap(evt -> shouldProcess(ctx, evt));
    }

    protected abstract Mono<Boolean> shouldProcess(EventProcessorContext ctx, MessageEvent event);

    /**
     * TBW.
     */
    @Override
    public Flux<EventResponse> process(EventProcessorContext ctx, Event event) {
        return Mono.just(event)
                   .filter(evt -> evt instanceof MessageEvent)
                   .cast(MessageEvent.class)
                   .flatMapMany(evt -> process(ctx, evt));
    }

    protected abstract Flux<EventResponse> process(EventProcessorContext ctx, MessageEvent event);
}
