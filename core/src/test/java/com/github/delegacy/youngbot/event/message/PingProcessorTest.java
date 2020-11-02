package com.github.delegacy.youngbot.event.message;

import java.util.Collections;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.github.delegacy.youngbot.event.EventResponse;
import com.github.delegacy.youngbot.event.EventService;

import reactor.test.StepVerifier;

class PingProcessorTest {
    private final PingProcessor processor = new PingProcessor();

    private final EventService eventService = new EventService(Collections.singleton(processor));

    @ParameterizedTest
    @ValueSource(strings = { "ping", "/ping", "PING", "/PING", "pInG", "/PiNg" })
    void testMatched(String input) throws Exception {
        StepVerifier.create(eventService.process(MessageEvent.of(input))
                                        .map(EventResponse::text))
                    .expectNext("PONG")
                    .expectComplete()
                    .verify();
    }

    @ParameterizedTest
    @ValueSource(strings = { "pang", "/pang", "PIMG", "/PIMG", "fInG", "/FiNg" })
    void testNotMatched(String input) {
        StepVerifier.create(eventService.process(MessageEvent.of(input)))
                    .expectComplete()
                    .verify();
    }
}
