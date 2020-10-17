package com.github.delegacy.youngbot.line;

import static java.util.Objects.requireNonNull;

import com.github.delegacy.youngbot.message.AbstractMessageRequest;
import com.google.common.base.MoreObjects;

/**
 * TBW.
 */
public class LineMessageRequest extends AbstractMessageRequest {
    private final String replyToken;

    /**
     * TBW.
     */
    public LineMessageRequest(String text, String channel, String replyToken) {
        super(text, channel);

        this.replyToken = requireNonNull(replyToken, "replyToken");
    }

    /**
     * TBW.
     */
    public String replyToken() {
        return replyToken;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                          .add("text", text())
                          .add("channel", channel())
                          .add("replyToken", replyToken)
                          .toString();
    }
}
