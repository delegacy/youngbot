package com.github.delegacy.youngbot.server.message;

import com.github.delegacy.youngbot.server.platform.Platform;

/**
 * TBW.
 */
public interface MessageContext {
    /**
     * TBW.
     */
    Platform platform();

    /**
     * TBW.
     */
    String text();

    /**
     * TBW.
     */
    String channelId();
}
