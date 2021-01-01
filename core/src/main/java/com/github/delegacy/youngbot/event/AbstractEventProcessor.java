package com.github.delegacy.youngbot.event;

import com.google.common.reflect.TypeToken;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * TBW.
 */
public abstract class AbstractEventProcessor<T extends Event> implements EventProcessor {
    @SuppressWarnings("UnstableApiUsage")
    private final TypeToken<T> type = new TypeToken<>(getClass()) {};

    @Override
    public Flux<EventResponse> process(Event event) {
        //noinspection UnstableApiUsage
        if (!type.getRawType().isInstance(event)) {
            return Flux.empty();
        }

        @SuppressWarnings("unchecked")
        final var cast = (T) event;
        return shouldProcess0(cast)
                .flatMapMany(b -> b ? process0(cast) : Flux.empty());
    }

    protected abstract Mono<Boolean> shouldProcess0(T event);

    protected abstract Flux<EventResponse> process0(T event);
}
