package com.github.delegacy.youngbot.server.message;

import com.github.delegacy.youngbot.server.platform.Platform;

public interface MessageContext {
    Platform platform();

    String text();

    String channelId();
}
