package com.github.delegacy.youngbot.server.web;

import java.util.Random;

import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import reactor.core.publisher.Mono;
import reactor.util.context.Context;

/**
 * TBW.
 */
@Component
public class ReactorContextFilter implements WebFilter {
    private static final Random RANDOM = new Random();

    public static final String REACTOR_CONTEXT_KEY = "REACTOR_CONTEXT";
    public static final String REQUEST_ID_KEY = "REQUEST_ID";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        final String requestId = Long.toHexString(RANDOM.nextLong());

        final Context context = Context.of(REQUEST_ID_KEY, requestId);
        exchange.getAttributes().put(REACTOR_CONTEXT_KEY, context);

        return chain.filter(exchange)
                    .subscriberContext(context);
    }
}