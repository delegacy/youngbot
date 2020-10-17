package com.github.delegacy.youngbot.web;

import com.github.delegacy.youngbot.message.MessageResponse;

/**
 * TBW.
 */
public class WebhookResponse {
    private final String text;

    /**
     * TBW.
     */
    public WebhookResponse(MessageResponse messageResponse) {
        text = messageResponse.text();
    }

    /**
     * TBW.
     */
    public String getText() {
        return text;
    }
}
