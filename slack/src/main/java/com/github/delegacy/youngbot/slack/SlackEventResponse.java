package com.github.delegacy.youngbot.slack;

import static java.util.Objects.requireNonNull;

import com.github.delegacy.youngbot.event.EventResponse;
import com.google.common.base.MoreObjects;

/**
 * TBW.
 */
public final class SlackEventResponse implements EventResponse {
    /**
     * TBW.
     */
    public static SlackEventResponse of(EventResponse eventResponse) {
        if (eventResponse instanceof SlackEventResponse) {
            return (SlackEventResponse) eventResponse;
        }

        return builder(requireNonNull(eventResponse, "eventResponse").text()).build();
    }

    /**
     * TBW.
     */
    public static SlackEventResponseBuilder builder(String text) {
        return new SlackEventResponseBuilder(requireNonNull(text, "text"));
    }

    private final String text;

    private final boolean secret;

    SlackEventResponse(String text, boolean secret) {
        this.text = text;
        this.secret = secret;
    }

    @Override
    public String text() {
        return text;
    }

    /**
     * TBW.
     */
    public boolean secret() {
        return secret;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                          .add("text", text)
                          .add("secret", secret)
                          .toString();
    }
}
