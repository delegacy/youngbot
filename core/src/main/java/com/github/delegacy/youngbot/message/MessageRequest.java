package com.github.delegacy.youngbot.message;

import static java.util.Objects.requireNonNull;

/**
 * TBW.
 */
public interface MessageRequest {
    /**
     * TBW.
     */
    static MessageRequest of(String channel, String text) {
        return new DefaultMessageRequest(requireNonNull(channel, "channel"),
                                         requireNonNull(text, "text"));
    }

    /**
     * TBW.
     */
    String channel();

    /**
     * TBW.
     */
    String text();
}
