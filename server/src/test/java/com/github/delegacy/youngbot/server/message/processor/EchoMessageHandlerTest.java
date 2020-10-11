package com.github.delegacy.youngbot.server.message.processor;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.regex.Matcher;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.github.delegacy.youngbot.server.message.MessageResponse;
import com.github.delegacy.youngbot.server.util.TestUtils;

import reactor.test.StepVerifier;

class EchoMessageHandlerTest {
    private final EchoMessageProcessor processor = new EchoMessageProcessor();

    @ParameterizedTest
    @MethodSource("provideStringsForTestMatched")
    void testMatched(String input, String expected) {
        final Matcher matcher = processor.pattern().matcher(input);
        assertThat(matcher.matches()).isEqualTo(true);
        StepVerifier.create(processor.process(TestUtils.msgReq(input), matcher)
                                     .map(MessageResponse::text))
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
        final Matcher matcher = processor.pattern().matcher(input);
        assertThat(matcher.matches()).isEqualTo(false);
    }
}
