package com.github.delegacy.youngbot.web;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * TBW.
 */
public class WebhookRequest {
    private final String text;

    /**
     * TBW.
     */
    @JsonCreator
    public WebhookRequest(@JsonProperty("text") String text) {
        this.text = requireNonNull(text, "text");
    }

    /**
     * TBW.
     */
    public String getText() {
        return text;
    }
}
