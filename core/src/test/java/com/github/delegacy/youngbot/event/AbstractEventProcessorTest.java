package com.github.delegacy.youngbot.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.github.delegacy.youngbot.event.message.MessageEvent;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class AbstractEventProcessorTest {
    private static final class HelloWorldProcessor extends AbstractEventProcessor<MessageEvent> {
        @Override
        protected Mono<Boolean> shouldProcess0(MessageEvent event) {
            return Mono.just(!event.text().isEmpty());
        }

        @Override
        protected Flux<EventResponse> process0(MessageEvent event) {
            return Flux.just(EventResponse.of("Hello, World!"));
        }
    }

    private static final class FailProcessor extends AbstractEventProcessor<MessageEvent> {
        @Override
        protected Mono<Boolean> shouldProcess0(MessageEvent event) {
            return Mono.error(new AssertionError("oops"));
        }

        @Override
        protected Flux<EventResponse> process0(MessageEvent event) {
            return Flux.error(new AssertionError("oops"));
        }
    }

    @Test
    void testMatched() throws Exception {
        final var p = new HelloWorldProcessor();
        StepVerifier.create(p.process(MessageEvent.of("test"))
                             .map(EventResponse::text))
                    .expectNext("Hello, World!")
                    .expectComplete()
                    .verify();
    }

    @Test
    void testNotMatched() throws Exception {
        final var p = new HelloWorldProcessor();
        StepVerifier.create(p.process(MessageEvent.of(""))
                             .map(EventResponse::text))
                    .expectComplete()
                    .verify();
    }

    @Test
    void testIncompatible(@Mock Event event) throws Exception {
        final var p = new FailProcessor();
        StepVerifier.create(p.process(event)
                             .map(EventResponse::text))
                    .expectComplete()
                    .verify();
    }
}
