package com.github.delegacy.youngbot.message.processor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.delegacy.youngbot.message.MessageRequest;
import com.github.delegacy.youngbot.message.MessageResponse;

import reactor.core.publisher.Flux;

/**
 * TBW.
 */
public interface MessageProcessor {
    /**
     * TBW.
     */
    Pattern pattern();

    /**
     * TBW.
     */
    Flux<MessageResponse> process(MessageRequest request, Matcher matcher);
}
