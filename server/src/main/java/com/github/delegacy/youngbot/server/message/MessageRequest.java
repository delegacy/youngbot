package com.github.delegacy.youngbot.server.message;

import static java.util.Objects.requireNonNull;

/**
 * TBW.
 */
public interface MessageRequest {
    /**
     * TBW.
     */
    static MessageRequest of(String text, String channel) {
        return new DefaultMessageRequest(requireNonNull(text, "text"),
                                         requireNonNull(channel, "channel"));
    }

    /**
     * TBW.
     */
    String text();

    /**
     * TBW.
     */
    String channel();
}
