package com.github.delegacy.youngbot.slack;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.InetSocketAddress;

import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;

class AbstractSlackControllerStaticTest {
    @Test
    void testToRemoteAddress() throws Exception {
        var req = MockServerHttpRequest.post("/path/to/webhook").build();
        assertThat(AbstractSlackController.toRemoteAddress(req)).isNull();

        req = MockServerHttpRequest.post("/path/to/webhook")
                                   .remoteAddress(new InetSocketAddress(8080))
                                   .build();
        assertThat(AbstractSlackController.toRemoteAddress(req)).isEqualTo("0.0.0.0");

        req = MockServerHttpRequest.post("/path/to/webhook")
                                   .remoteAddress(new InetSocketAddress("unresolved", 8080))
                                   .build();
        assertThat(AbstractSlackController.toRemoteAddress(req)).isNull();
    }
}
