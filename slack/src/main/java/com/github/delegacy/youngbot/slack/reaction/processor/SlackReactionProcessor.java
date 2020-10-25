package com.github.delegacy.youngbot.slack.reaction.processor;

import com.github.delegacy.youngbot.slack.reaction.SlackReactionRequest;
import com.github.delegacy.youngbot.slack.reaction.SlackReactionResponse;

import reactor.core.publisher.Flux;

/**
 * TBW.
 */
public interface SlackReactionProcessor {
    /**
     * TBW.
     */
    boolean matches(SlackReactionRequest request);

    /**
     * TBW.
     */
    Flux<SlackReactionResponse> process(SlackReactionRequest request);
}
