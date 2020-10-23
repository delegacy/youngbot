package com.github.delegacy.youngbot.slack.reaction;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;

/**
 * TBW.
 */
public final class SlackReactionResponse {
    /**
     * TBW.
     */
    public static SlackReactionResponse of(SlackReactionRequest request, String text) {
        return new SlackReactionResponse(requireNonNull(request, "request"),
                                         requireNonNull(text, "text"));
    }

    private final SlackReactionRequest request;

    private final String text;

    private SlackReactionResponse(SlackReactionRequest request, String text) {
        this.request = request;
        this.text = text;
    }

    /**
     * TBW.
     */
    public SlackReactionRequest request() {
        return request;
    }

    /**
     * TBW.
     */
    public String text() {
        return text;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                          .add("request", request)
                          .add("text", text)
                          .toString();
    }
}
