package com.github.delegacy.youngbot.event;

class DefaultEventResponse implements EventResponse {
    private final String text;

    DefaultEventResponse(String text) {
        this.text = text;
    }

    @Override
    public String text() {
        return text;
    }
}
