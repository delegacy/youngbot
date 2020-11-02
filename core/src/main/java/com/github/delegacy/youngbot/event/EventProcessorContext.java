package com.github.delegacy.youngbot.event;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TBW.
 */
public final class EventProcessorContext {
    private final EventProcessor processor;

    private final Event event;

    private final Map<String, Object> attrs = new ConcurrentHashMap<>();

    EventProcessorContext(EventProcessor processor, Event event) {
        this.processor = processor;
        this.event = event;
    }

    /**
     * TBW.
     */
    public EventProcessor processor() {
        return processor;
    }

    /**
     * TBW.
     */
    public Event event() {
        return event;
    }

    /**
     * TBW.
     */
    public Map<String, Object> attrs() {
        return attrs;
    }
}
