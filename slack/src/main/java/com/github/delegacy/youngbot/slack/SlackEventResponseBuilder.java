package com.github.delegacy.youngbot.slack;

/**
 * TBW.
 */
public final class SlackEventResponseBuilder {
    private final String text;

    private boolean ephemeral;

    /**
     * TBW.
     */
    SlackEventResponseBuilder(String text) {
        this.text = text;
    }

    /**
     * TBW.
     */
    public SlackEventResponseBuilder ephemeral(boolean ephemeral) {
        this.ephemeral = ephemeral;
        return this;
    }

    /**
     * TBW.
     */
    public SlackEventResponse build() {
        return new SlackEventResponse(text, ephemeral);
    }
}
