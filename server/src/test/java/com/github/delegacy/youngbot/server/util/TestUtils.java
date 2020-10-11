package com.github.delegacy.youngbot.server.util;

import com.github.delegacy.youngbot.server.message.MessageRequest;

public final class TestUtils {
    public static MessageRequest msgReq(String text) {
        return MessageRequest.of(text, "test");
    }

    private TestUtils() { /* noop */ }
}
