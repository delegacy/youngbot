package com.github.delegacy.youngbot.slack.reaction;

import static java.util.Objects.requireNonNull;

import java.util.Set;

import com.github.delegacy.youngbot.slack.reaction.processor.SlackReactionProcessor;

import reactor.core.publisher.Flux;

/**
 * TBW.
 */
public class SlackReactionService {
    private final Set<SlackReactionProcessor> processors;

    /**
     * TBW.
     */
    public SlackReactionService(Set<SlackReactionProcessor> processors) {
        this.processors = requireNonNull(processors, "processors");
    }

    /**
     * TBW.
     */
    public Flux<SlackReactionResponse> process(SlackReactionRequest request) {
        return Flux.fromIterable(processors)
                   .filter(p -> p.matches(request))
                   .flatMap(p -> p.process(request))
                   .filter(res -> !res.text().isEmpty());
    }
}
