package com.github.delegacy.youngbot.slack;

/**
 * TBW.
 */
public final class SlackEventResponseBuilder {
    private final String text;

    private boolean secret;

    /**
     * TBW.
     */
    SlackEventResponseBuilder(String text) {
        this.text = text;
    }

    /**
     * TBW.
     */
    public SlackEventResponseBuilder secret(boolean secret) {
        this.secret = secret;
        return this;
    }

    /**
     * TBW.
     */
    public SlackEventResponse build() {
        return new SlackEventResponse(text, secret);
    }
}
