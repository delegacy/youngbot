package com.github.delegacy.youngbot.server;

import org.springframework.web.server.ServerWebExchange;

import com.github.delegacy.youngbot.server.platform.Platform;

import lombok.Value;
import lombok.experimental.Accessors;

@Value
@Accessors(fluent = true)
public class RequestContext {
    private Platform platform;

    private ServerWebExchange exchange;

    private String text;

    private String replyTo;
}
