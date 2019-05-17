package com.github.delegacy.youngbot.server;

import java.util.Random;

import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import com.linecorp.armeria.common.RequestContext;

import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

@Slf4j
@Component
public class RequestIdFilter implements WebFilter {
    private static final Random RANDOM = new Random();
    private static final String REQUEST_ID_KEY = "REQUEST_ID";
    private static final AttributeKey<String> REQUEST_ID_ATTR_KEY =
            AttributeKey.valueOf(RequestIdFilter.class, REQUEST_ID_KEY);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        final String requestId = Long.toHexString(RANDOM.nextLong());
        try {
            final RequestContext reqCtx = RequestContext.current();
            reqCtx.attr(REQUEST_ID_ATTR_KEY).set(requestId);
        } catch (RuntimeException e) {
            log.warn("Failed to set requestId<{}>", requestId);
        }

        return chain.filter(exchange)
                    .subscriberContext(Context.of(REQUEST_ID_KEY, requestId));
    }
}
