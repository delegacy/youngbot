package com.github.delegacy.youngbot.message;

class DefaultMessageResponse implements MessageResponse {
    private final MessageRequest request;

    private final String text;

    DefaultMessageResponse(MessageRequest request, String text) {
        this.request = request;
        this.text = text;
    }

    @Override
    public MessageRequest request() {
        return request;
    }

    @Override
    public String text() {
        return text;
    }
}
