package com.github.delegacy.youngbot.event.message;

import static java.util.Objects.requireNonNull;

import com.github.delegacy.youngbot.event.Event;

/**
 * TBW.
 */
@SuppressWarnings("InterfaceMayBeAnnotatedFunctional")
public interface MessageEvent extends Event {
    /**
     * TBW.
     */
    static MessageEvent of(String text) {
        return new DefaultMessageEvent(requireNonNull(text, "text"));
    }

    /**
     * TBW.
     */
    String text();
}
