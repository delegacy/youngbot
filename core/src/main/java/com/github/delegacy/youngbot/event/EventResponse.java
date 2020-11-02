package com.github.delegacy.youngbot.event;

import static java.util.Objects.requireNonNull;

/**
 * TBW.
 */
@SuppressWarnings("InterfaceMayBeAnnotatedFunctional")
public interface EventResponse {
    /**
     * TBW.
     */
    static EventResponse of(String text) {
        return new DefaultEventResponse(requireNonNull(text, "text"));
    }

    /**
     * TBW.
     */
    String text();
}
