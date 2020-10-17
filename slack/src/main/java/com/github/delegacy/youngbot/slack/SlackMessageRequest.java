package com.github.delegacy.youngbot.slack;

import javax.annotation.Nullable;

import com.github.delegacy.youngbot.message.AbstractMessageRequest;
import com.google.common.base.MoreObjects;

/**
 * TBW.
 */
public class SlackMessageRequest extends AbstractMessageRequest {
    @Nullable
    private final String thread;

    /**
     * TBW.
     */
    public SlackMessageRequest(String text, String channel) {
        this(text, channel, null);
    }

    /**
     * TBW.
     */
    public SlackMessageRequest(String text, String channel, @Nullable String thread) {
        super(text, channel);

        this.thread = thread;
    }

    /**
     * TBW.
     */
    @Nullable
    public String thread() {
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
