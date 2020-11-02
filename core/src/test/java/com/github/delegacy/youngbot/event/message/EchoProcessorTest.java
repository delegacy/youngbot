package com.github.delegacy.youngbot.event.message;

import java.util.Collections;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.github.delegacy.youngbot.event.EventResponse;
import com.github.delegacy.youngbot.event.EventService;

import reactor.test.StepVerifier;

class EchoProcessorTest {
    private final EchoProcessor processor = new EchoProcessor();

    private final EventService eventService = new EventService(Collections.singleton(processor));

    @ParameterizedTest
    @MethodSource("provideStringsForTestMatched")
    void testMatched(String input, String expected) {
        StepVerifier.create(eventService.process(MessageEvent.of(input))
                                        .map(EventResponse::text))
                    .expectNext(expected)
                    .expectComplete()
                    .verify();
    }

    private static Stream<Arguments> provideStringsForTestMatched() {
        return Stream.of(
                Arguments.of("echo Hello", "Hello"),
                Arguments.of("/echo Hello", "Hello"),
                Arguments.of("/echo   Hello", "Hello"),
                Arguments.of("/echo 안녕하세요", "안녕하세요"),
                Arguments.of("/echo こんにちは", "こんにちは"),
                Arguments.of("/echo Hello Hello", "Hello Hello"),
                Arguments.of("/echo Hello\nHello", "Hello\nHello")
        );
    }

    @ParameterizedTest
    @ValueSource(strings = { "eko Hello", "/eko Hello" })
    void testNotMatched(String input) {
        StepVerifier.create(eventService.process(MessageEvent.of(input)))
                    .expectComplete()
                    .verify();
    }
}
