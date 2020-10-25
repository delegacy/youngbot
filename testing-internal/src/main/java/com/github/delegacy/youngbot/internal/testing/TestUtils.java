package com.github.delegacy.youngbot.internal.testing;

import com.github.delegacy.youngbot.message.MessageRequest;

public final class TestUtils {
    public static MessageRequest msgReq(String text) {
        return MessageRequest.of("channel", text);
    }

    private TestUtils() { /* noop */ }
}
