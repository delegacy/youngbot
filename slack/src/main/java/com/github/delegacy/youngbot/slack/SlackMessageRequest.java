package com.github.delegacy.youngbot.slack;

import static com.google.common.base.MoreObjects.firstNonNull;
import static java.util.Objects.requireNonNull;

import com.github.delegacy.youngbot.message.AbstractMessageRequest;
import com.google.common.base.MoreObjects;
import com.slack.api.model.event.MessageEvent;

/**
 * TBW.
 */
public final class SlackMessageRequest extends AbstractMessageRequest {
    /**
     * TBW.
     */
    public static SlackMessageRequest of(MessageEvent messageEvent) {
        requireNonNull(messageEvent, "messageEvent");

        return new SlackMessageRequest(messageEvent.getText(), messageEvent.getChannel(),
                                       firstNonNull(messageEvent.getThreadTs(), messageEvent.getTs()));
    }

    private final String thread;

    private SlackMessageRequest(String text, String channel, String thread) {
        super(text, channel);

        this.thread = thread;
    }

    /**
     * TBW.
     */
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
