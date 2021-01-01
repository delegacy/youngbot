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

    private final boolean ephemeral;

    SlackEventResponse(String text, boolean ephemeral) {
        this.text = text;
        this.ephemeral = ephemeral;
    }

    @Override
    public String text() {
        return text;
    }

    /**
     * TBW.
     */
    public boolean ephemeral() {
        return ephemeral;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                          .add("text", text)
                          .add("ephemeral", ephemeral)
                          .toString();
    }
}
