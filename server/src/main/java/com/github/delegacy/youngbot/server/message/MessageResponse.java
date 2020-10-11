package com.github.delegacy.youngbot.server.message;

import static java.util.Objects.requireNonNull;

/**
 * TBW.
 */
public interface MessageResponse {
    /**
     * TBW.
     */
    static MessageResponse of(MessageRequest request, String text) {
        return new DefaultMessageResponse(requireNonNull(request, "request"),
                                          requireNonNull(text, "text"));
    }

    /**
     * TBW.
     */
    MessageRequest request();

    /**
     * TBW.
     */
    String text();
}
