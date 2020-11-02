package com.github.delegacy.youngbot.slack;

/**
 * TBW.
 */
public interface SlackReplyableEvent extends SlackEvent {
    /**
     * TBW.
     */
    String channel();

    /**
     * TBW.
     */
    String user();

    /**
     * TBW.
     */
    String ts();
}
