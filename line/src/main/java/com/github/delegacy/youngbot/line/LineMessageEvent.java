package com.github.delegacy.youngbot.line;

import static java.util.Objects.requireNonNull;

import com.github.delegacy.youngbot.event.message.MessageEvent;
import com.google.common.base.MoreObjects;

/**
 * TBW.
 */
public class LineMessageEvent implements MessageEvent,
                                         LineReplyableEvent {
    private final String channel;

    private final String text;

    private final String replyToken;

    /**
     * TBW.
     */
    public LineMessageEvent(String channel, String text, String replyToken) {
        this.channel = requireNonNull(channel, "channel");
        this.text = requireNonNull(text, "text");
        this.replyToken = requireNonNull(replyToken, "replyToken");
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
