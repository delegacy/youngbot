package com.github.delegacy.youngbot.server.line;

import static java.util.Objects.requireNonNull;

import com.github.delegacy.youngbot.server.message.AbstractMessageContext;
import com.github.delegacy.youngbot.server.platform.Platform;

class LineMessageContext extends AbstractMessageContext {
    private final String replyToken;

    LineMessageContext(String text, String replyToken, String channelId) {
        super(text, channelId);

        this.replyToken = requireNonNull(replyToken, "replyToken");
    }

    @Override
    public Platform platform() {
        return Platform.LINE;
    }

    String replyToken() {
        return replyToken;
    }
}
