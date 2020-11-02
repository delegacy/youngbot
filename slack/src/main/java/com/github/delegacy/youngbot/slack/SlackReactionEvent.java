package com.github.delegacy.youngbot.slack;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import com.slack.api.model.event.ReactionAddedEvent;

/**
 * TBW.
 */
public final class SlackReactionEvent implements SlackReplyableEvent {
    /**
     * TBW.
     */
    public static SlackReactionEvent of(ReactionAddedEvent event) {
        requireNonNull(event, "event");

        return new SlackReactionEvent(event.getItem().getChannel(), event.getReaction(),
                                      event.getUser(), event.getItem().getTs());
    }

    private final String channel;

    private final String reaction;

    private final String user;

    private final String ts;

    @VisibleForTesting
    SlackReactionEvent(String channel, String reaction, String user, String ts) {
        this.channel = channel;
        this.reaction = reaction;
        this.user = user;
        this.ts = ts;
    }

    @Override
    public String channel() {
        return channel;
    }

    /**
     * TBW.
     */
    public String reaction() {
        return reaction;
    }

    @Override
    public String user() {
        return user;
    }

    @Override
    public String ts() {
        return ts;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                          .add("channel", channel)
                          .add("reaction", reaction)
                          .add("user", user)
                          .add("ts", ts)
                          .toString();
    }
}
