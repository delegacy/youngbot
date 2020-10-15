package com.github.delegacy.youngbot.server.message.service;

import static java.util.Objects.requireNonNull;

import java.util.Set;
import java.util.regex.Matcher;

import org.springframework.stereotype.Service;

import com.github.delegacy.youngbot.server.message.MessageRequest;
import com.github.delegacy.youngbot.server.message.MessageResponse;
import com.github.delegacy.youngbot.server.message.processor.MessageProcessor;

import reactor.core.publisher.Flux;

/**
 * TBW.
 */
@Service
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
