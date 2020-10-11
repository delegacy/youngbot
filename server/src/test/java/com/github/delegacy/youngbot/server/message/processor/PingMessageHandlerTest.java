package com.github.delegacy.youngbot.server.message.processor;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.regex.Matcher;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.github.delegacy.youngbot.server.message.MessageResponse;
import com.github.delegacy.youngbot.server.util.TestUtils;

import reactor.test.StepVerifier;

class PingMessageHandlerTest {
    private final PingMessageProcessor processor = new PingMessageProcessor();

    @ParameterizedTest
    @ValueSource(strings = { "ping", "/ping", "PING", "/PING", "pInG", "/PiNg" })
    void testMatched(String input) {
        final Matcher matcher = processor.pattern().matcher(input);
        assertThat(matcher.matches()).isEqualTo(true);
        StepVerifier.create(processor.process(TestUtils.msgReq(input), matcher)
                                     .map(MessageResponse::text))
                    .expectNext("PONG")
                    .expectComplete()
                    .verify();
    }

    @ParameterizedTest
    @ValueSource(strings = { "pang", "/pang", "PIMG", "/PIMG", "fInG", "/FiNg" })
    void testNotMatched(String input) {
        final Matcher matcher = processor.pattern().matcher(input);
        assertThat(matcher.matches()).isEqualTo(false);
    }
}
