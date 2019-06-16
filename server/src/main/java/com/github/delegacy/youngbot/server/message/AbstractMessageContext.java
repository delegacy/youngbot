package com.github.delegacy.youngbot.server.message;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;

public abstract class AbstractMessageContext implements MessageContext {
    private final String text;

    private final String channelId;

    protected AbstractMessageContext(String text, String channelId) {
        this.text = requireNonNull(text, "text");
        this.channelId = requireNonNull(channelId, "channelId");
    }

    @Override
    public String text() {
        return text;
    }

    @Override
    public String channelId() {
        return channelId;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                          .add("text", text)
                          .add("channelId", channelId)
                          .toString();
    }
}
