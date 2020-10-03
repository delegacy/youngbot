package com.github.delegacy.youngbot.server.message.handler;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.delegacy.youngbot.server.message.MessageContext;

import reactor.core.publisher.Flux;

/**
 * TBW.
 */
public interface MessageHandler {
    /**
     * TBW.
     */
    Pattern pattern();

    /**
     * TBW.
     */
    Flux<String> handle(MessageContext msgCtx, Matcher matcher);
}
