package com.github.delegacy.youngbot.slack;

import com.github.delegacy.youngbot.event.Event;
import com.github.delegacy.youngbot.event.EventProcessor;
import com.github.delegacy.youngbot.event.EventProcessorContext;
import com.github.delegacy.youngbot.event.EventResponse;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * TBW.
 */
public abstract class AbstractSlackReactionProcessor implements EventProcessor {
    /**
     * TBW.
     */
    @Override
    public Mono<Boolean> shouldProcess(EventProcessorContext ctx, Event event) {
        return Mono.just(event)
                   .filter(evt -> evt instanceof SlackReactionEvent)
                   .cast(SlackReactionEvent.class)
                   .flatMap(evt -> shouldProcess(ctx, evt));
    }

    protected abstract Mono<Boolean> shouldProcess(EventProcessorContext ctx, SlackReactionEvent event);

    /**
     * TBW.
     */
    @Override
    public Flux<EventResponse> process(EventProcessorContext ctx, Event event) {
        return Mono.just(event)
                   .filter(evt -> evt instanceof SlackReactionEvent)
                   .cast(SlackReactionEvent.class)
                   .flatMapMany(evt -> process(ctx, evt));
    }

    protected abstract Flux<EventResponse> process(EventProcessorContext ctx, SlackReactionEvent event);
}
