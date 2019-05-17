package com.github.delegacy.youngbot.server;

import org.springframework.http.HttpMethod;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;

import com.github.delegacy.youngbot.server.platform.Platform;

public final class RequestContextTestUtils {
    public static RequestContext newRequestContext(String text) {
        return newRequestContext(text, "replyTo");
    }

    public static RequestContext newRequestContext(String text, String replyTo) {
        return new RequestContext(Platform.LINE,
                                  MockServerWebExchange.from(
                                          MockServerHttpRequest.method(HttpMethod.GET,
                                                                       "https://example.com")),
                                  text, replyTo);

    }

    private RequestContextTestUtils() {
        // do nothing
    }
}
