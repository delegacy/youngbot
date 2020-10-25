package com.github.delegacy.youngbot.message;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;

/**
 * TBW.
 */
public abstract class AbstractMessageRequest implements MessageRequest {
    private final String channel;

    private final String text;

    protected AbstractMessageRequest(String channel, String text) {
        this.channel = requireNonNull(channel, "channel");
        this.text = requireNonNull(text, "text");
    }

    @Override
    public String channel() {
        return channel;
    }

    @Override
    public String text() {
        return text;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                          .add("channel", channel)
                          .add("text", text)
                          .toString();
    }
}
