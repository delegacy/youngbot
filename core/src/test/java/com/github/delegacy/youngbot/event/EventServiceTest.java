package com.github.delegacy.youngbot.event;

import java.util.Set;

import org.junit.jupiter.api.Test;

import com.github.delegacy.youngbot.event.message.MessageEvent;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class EventServiceTest {
    private static final class DotProcessor extends AbstractEventProcessor<MessageEvent> {
        @Override
        protected Mono<Boolean> shouldProcess0(MessageEvent event) {
            return Mono.just(true);
        }

        @Override
        protected Flux<EventResponse> process0(MessageEvent event) {
            return Flux.just(EventResponse.of("."));
        }
    }

    private static final class EmptyProcessor extends AbstractEventProcessor<MessageEvent> {
        @Override
        protected Mono<Boolean> shouldProcess0(MessageEvent event) {
            return Mono.just(true);
        }

        @Override
        protected Flux<EventResponse> process0(MessageEvent event) {
            return Flux.empty();
        }
    }

    private static final class StringEmptyProcessor extends AbstractEventProcessor<MessageEvent> {
        @Override
        protected Mono<Boolean> shouldProcess0(MessageEvent event) {
            return Mono.just(true);
        }

        @Override
        protected Flux<EventResponse> process0(MessageEvent event) {
            return Flux.just(EventResponse.of(""));
        }
    }

    private final EventService eventService = new EventService(Set.of(new DotProcessor(),
                                                                      new EmptyProcessor(),
                                                                      new StringEmptyProcessor()));

    @Test
    void testProcess() throws Exception {
        StepVerifier.create(eventService.process(MessageEvent.of("..."))
                                        .map(EventResponse::text))
                    .expectNext(".")
                    .expectComplete()
                    .verify();
    }
}
