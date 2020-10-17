package com.github.delegacy.youngbot.message;

import static java.util.Objects.requireNonNull;

import java.util.Set;
import java.util.regex.Matcher;

import com.github.delegacy.youngbot.message.processor.MessageProcessor;

import reactor.core.publisher.Flux;

/**
 * TBW.
 */
public class MessageService {
    private final Set<MessageProcessor> processors;

    /**
     * TBW.
     */
    public MessageService(Set<MessageProcessor> processors) {
        this.processors = requireNonNull(processors, "processors");
    }

    /**
     * TBW.
     */
    public Flux<MessageResponse> process(MessageRequest request) {
        if (request.text().isEmpty()) {
            return Flux.empty();
        }

        return Flux.fromIterable(processors)
                   .flatMap(p -> {
                       final Matcher matcher = p.pattern().matcher(request.text());
                       if (!matcher.matches()) {
                           return Flux.empty();
                       }
                       return p.process(request, matcher);
                   })
                   .filter(res -> !res.text().isEmpty());
    }
}
