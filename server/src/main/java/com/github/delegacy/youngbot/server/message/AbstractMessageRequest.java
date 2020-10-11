package com.github.delegacy.youngbot.server.message;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;

/**
 * TBW.
 */
public abstract class AbstractMessageRequest implements MessageRequest {
    private final String text;

    private final String channel;

    protected AbstractMessageRequest(String text, String channel) {
        this.text = requireNonNull(text, "text");
        this.channel = requireNonNull(channel, "channel");
    }

    @Override
    public String text() {
        return text;
    }

    @Override
    public String channel() {
        return channel;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                          .add("text", text)
                          .add("channel", channel)
                          .toString();
    }
}
