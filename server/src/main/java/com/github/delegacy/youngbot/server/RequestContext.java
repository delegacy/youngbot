package com.github.delegacy.youngbot.server;

import com.github.delegacy.youngbot.server.platform.Platform;

public class RequestContext {
    private Platform platform;

    private String text;

    private String replyTo;

    public Platform platform() {
        return platform;
    }

    public void platform(Platform platform) {
        this.platform = platform;
    }

    public String text() {
        return text;
    }

    public void text(String text) {
        this.text = text;
    }

    public String replyTo() {
        return replyTo;
    }

    public void replyTo(String replyTo) {
        this.replyTo = replyTo;
    }
}
