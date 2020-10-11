package com.github.delegacy.youngbot.server.slack;

import javax.annotation.Nullable;

import com.github.delegacy.youngbot.server.message.AbstractMessageRequest;
import com.google.common.base.MoreObjects;

class SlackMessageRequest extends AbstractMessageRequest {
    @Nullable
    private final String thread;

    SlackMessageRequest(String text, String channel) {
        this(text, channel, null);
    }

    SlackMessageRequest(String text, String channel, @Nullable String thread) {
        super(text, channel);

        this.thread = thread;
    }

    @Nullable
    String thread() {
        return thread;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                          .add("text", text())
                          .add("channel", channel())
                          .add("thread", thread)
                          .toString();
    }
}
