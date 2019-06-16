package com.github.delegacy.youngbot.server.util;

import com.github.delegacy.youngbot.server.message.MessageContext;
import com.github.delegacy.youngbot.server.platform.Platform;

public final class MessageContextTestUtils {
    public static MessageContext newMessageContext(String text) {
        return newMessageContext(text, "aChannelId");
    }

    public static MessageContext newMessageContext(String text, String channelId) {
        return new MessageContext() {
            @Override
            public Platform platform() {
                return Platform.LINE;
            }

            @Override
            public String text() {
                return text;
            }

            @Override
            public String channelId() {
                return channelId;
            }
        };
    }

    private MessageContextTestUtils() {}
}
