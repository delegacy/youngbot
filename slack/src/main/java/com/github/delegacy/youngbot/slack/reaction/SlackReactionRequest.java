package com.github.delegacy.youngbot.slack.reaction;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import com.slack.api.model.event.ReactionAddedEvent;

/**
 * TBW.
 */
public final class SlackReactionRequest {
    /**
     * TBW.
     */
    public static SlackReactionRequest of(ReactionAddedEvent event) {
        requireNonNull(event, "event");

        return new SlackReactionRequest(event.getItem().getChannel(), event.getReaction(),
                                        event.getUser(), event.getItem().getTs());
    }

    private final String channel;

    private final String reaction;

    private final String user;

    private final String messageTs;

    @VisibleForTesting
    SlackReactionRequest(String channel, String reaction, String user, String messageTs) {
        this.channel = channel;
        this.reaction = reaction;
        this.user = user;
        this.messageTs = messageTs;
    }

    /**
     * TBW.
     */
    public String channel() {
        return channel;
    }

    /**
     * TBW.
     */
    public String reaction() {
        return reaction;
    }

    /**
     * TBW.
     */
    public String user() {
        return user;
    }

    /**
     * TBW.
     */
    public String messageTs() {
        return messageTs;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                          .add("channel", channel)
                          .add("reaction", reaction)
                          .add("user", user)
                          .add("messageTs", messageTs)
                          .toString();
    }
}
