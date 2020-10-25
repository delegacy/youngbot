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

        return new SlackMessageRequest(messageEvent.getChannel(), messageEvent.getText(),
                                       firstNonNull(messageEvent.getThreadTs(), messageEvent.getTs()));
    }

    private final String threadTs;

    private SlackMessageRequest(String channel, String text, String threadTs) {
        super(channel, text);

        this.threadTs = threadTs;
    }

    /**
     * TBW.
     */
    public String threadTs() {
        return threadTs;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                          .add("channel", channel())
                          .add("text", text())
                          .add("threadTs", threadTs)
                          .toString();
    }
}
