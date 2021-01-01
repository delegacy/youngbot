package com.github.delegacy.youngbot.line;

import java.util.Objects;

import com.linecorp.bot.parser.WebhookParseException;

/**
 * TBW.
 */
public class UncheckedWebhookParseException extends RuntimeException {
    private static final long serialVersionUID = -7587349414016848817L;

    /**
     * TBW.
     */
    public UncheckedWebhookParseException(String message, WebhookParseException cause) {
        super(message, Objects.requireNonNull(cause));
    }

    /**
     * TBW.
     */
    public UncheckedWebhookParseException(WebhookParseException cause) {
        super(Objects.requireNonNull(cause));
    }

    @Override
    public WebhookParseException getCause() {
        return (WebhookParseException) super.getCause();
    }
}
