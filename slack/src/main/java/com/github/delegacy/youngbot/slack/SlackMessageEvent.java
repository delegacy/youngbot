package com.github.delegacy.youngbot.slack;

import static com.google.common.base.MoreObjects.firstNonNull;
import static java.util.Objects.requireNonNull;

import com.github.delegacy.youngbot.event.message.MessageEvent;
import com.google.common.base.MoreObjects;

/**
 * TBW.
 */
public final class SlackMessageEvent implements MessageEvent,
                                                SlackReplyableEvent {
    /**
     * TBW.
     */
    public static SlackMessageEvent of(com.slack.api.model.event.MessageEvent messageEvent) {
        requireNonNull(messageEvent, "messageEvent");

        return of(messageEvent.getChannel(), messageEvent.getText(), messageEvent.getUser(),
                  firstNonNull(messageEvent.getThreadTs(), messageEvent.getTs()));
    }

    /**
     * TBW.
     */
    public static SlackMessageEvent of(String channel, String text, String user, String threadTs) {
        return new SlackMessageEvent(requireNonNull(channel, "channel"),
                                     requireNonNull(text, "text"),
                                     requireNonNull(user, "user"),
                                     requireNonNull(threadTs, "threadTs"));
    }

    private final String channel;

    private final String text;

    private final String user;

    private final String threadTs;

    private SlackMessageEvent(String channel, String text, String user, String threadTs) {
        this.channel = channel;
        this.text = text;
        this.user = user;
        this.threadTs = threadTs;
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
    public String user() {
        return user;
    }

    @Override
    public String ts() {
        return threadTs;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                          .add("channel", channel)
                          .add("text", text())
                          .add("user", user)
                          .add("threadTs", threadTs)
                          .toString();
    }
}
