package com.github.delegacy.youngbot.server.slack;

import com.github.delegacy.youngbot.server.message.AbstractMessageContext;
import com.github.delegacy.youngbot.server.platform.Platform;

public class SlackMessageContext extends AbstractMessageContext {
    SlackMessageContext(String text, String channelId) {
        super(text, channelId);
    }

    @Override
    public Platform platform() {
        return Platform.SLACK;
    }
}
