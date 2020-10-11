package com.github.delegacy.youngbot.server.line;

import static java.util.Objects.requireNonNull;

import com.github.delegacy.youngbot.server.message.AbstractMessageRequest;
import com.google.common.base.MoreObjects;

class LineMessageRequest extends AbstractMessageRequest {
    private final String replyToken;

    LineMessageRequest(String text, String channel, String replyToken) {
        super(text, channel);

        this.replyToken = requireNonNull(replyToken, "replyToken");
    }

    String replyToken() {
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
