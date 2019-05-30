package com.github.delegacy.youngbot.server.message.handler;

import static com.github.delegacy.youngbot.server.RequestContextTestUtils.newRequestContext;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.regex.Matcher;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import reactor.test.StepVerifier;

class PingMessageHandlerTest {
    private final PingMessageHandler handler = new PingMessageHandler();

    @ParameterizedTest
    @ValueSource(strings = { "ping", "/ping", "PING", "/PING", "pInG", "/PiNg" })
    void testMatched(String input) {
        final Matcher matcher = handler.pattern().matcher(input);
        assertThat(matcher.matches()).isEqualTo(true);
        StepVerifier.create(handler.process(newRequestContext(input), input, matcher))
                    .expectNext("PONG")
                    .expectComplete()
                    .verify();
    }

    @ParameterizedTest
    @ValueSource(strings = { "pang", "/pang", "PIMG", "/PIMG", "fInG", "/FiNg" })
    void testNotMatched(String input) {
        final Matcher matcher = handler.pattern().matcher(input);
        assertThat(matcher.matches()).isEqualTo(false);
    }
}
