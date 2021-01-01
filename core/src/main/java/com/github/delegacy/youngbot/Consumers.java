package com.github.delegacy.youngbot;

import java.util.function.Consumer;

/**
 * TBW.
 */
public final class Consumers {
    /**
     * TBW.
     */
    public static <T> Consumer<T> noop() {
        return t -> {};
    }

    private Consumers() {}
}
