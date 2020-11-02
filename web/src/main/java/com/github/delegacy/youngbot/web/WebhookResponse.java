package com.github.delegacy.youngbot.web;

import com.github.delegacy.youngbot.event.EventResponse;

/**
 * TBW.
 */
public class WebhookResponse {
    private final String text;

    /**
     * TBW.
     */
    public WebhookResponse(EventResponse eventResponse) {
        text = eventResponse.text();
    }

    /**
     * TBW.
     */
    public String getText() {
        return text;
    }
}
