package com.github.delegacy.youngbot.line;

import static java.util.Objects.requireNonNull;

import com.github.delegacy.youngbot.event.message.MessageEvent;
import com.google.common.base.MoreObjects;

/**
 * TBW.
 */
public final class LineMessageEvent implements MessageEvent,
                                               LineReplyableEvent {
    /**
     * TBW.
     */
    public static LineMessageEvent of(String channel, String text, String replyToken) {
        return new LineMessageEvent(requireNonNull(channel, "channel"),
                                    requireNonNull(text, "text"),
                                    requireNonNull(replyToken, "replyToken"));
    }

    private final String channel;

    private final String text;

    private final String replyToken;

    private LineMessageEvent(String channel, String text, String replyToken) {
        this.channel = channel;
        this.text = text;
        this.replyToken = replyToken;
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
    public String replyToken() {
        return replyToken;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                          .add("channel", channel())
                          .add("text", text())
                          .add("replyToken", replyToken)
                          .toString();
    }
}
