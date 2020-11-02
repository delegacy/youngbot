package com.github.delegacy.youngbot.event.message;

class DefaultMessageEvent implements MessageEvent {
    private final String text;

    DefaultMessageEvent(String text) {
        this.text = text;
    }

    @Override
    public String text() {
        return text;
    }
}
