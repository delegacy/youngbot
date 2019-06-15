package com.github.delegacy.youngbot.server;

import static java.util.Objects.requireNonNull;

import org.springframework.web.server.ServerWebExchange;

import com.github.delegacy.youngbot.server.platform.Platform;
import com.google.common.base.MoreObjects;

public class RequestContext {
    private final Platform platform;

    private final ServerWebExchange exchange;

    private final String text;

    private final String replyTo;

    private final String channelId;

    public RequestContext(Platform platform, ServerWebExchange exchange,
                          String text, String replyTo, String channelId) {

        this.platform = requireNonNull(platform, "platform");
        this.exchange = requireNonNull(exchange, "exchange");
        this.text = requireNonNull(text, "text");
        this.replyTo = requireNonNull(replyTo, "replyTo");
        this.channelId = requireNonNull(channelId, "channelId");
    }

    public Platform platform() {
        return platform;
    }

    public ServerWebExchange exchange() {
        return exchange;
    }

    public String text() {
        return text;
    }

    public String replyTo() {
        return replyTo;
    }

    public String channelId() {
        return channelId;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                          .add("platform", platform)
                          .add("exchange", exchange)
                          .add("text", text)
                          .add("replyTo", replyTo)
                          .add("channelId", channelId)
                          .toString();
    }
}
