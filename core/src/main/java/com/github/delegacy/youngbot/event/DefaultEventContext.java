package com.github.delegacy.youngbot.event;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

final class DefaultEventContext implements EventContext {
    private final Event event;

    private final Map<Object, Object> attrs = new ConcurrentHashMap<>();

    /**
     * TBW.
     */
    DefaultEventContext(Event event) {
        this.event = event;
    }

    @Override
    public Event event() {
        return event;
    }

    @Override
    public Map<Object, Object> attrs() {
        return attrs;
    }
}
