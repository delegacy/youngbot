package com.github.delegacy.youngbot.server.message.handler;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.delegacy.youngbot.server.RequestContext;

import reactor.core.publisher.Flux;

public interface MessageHandler {
    Pattern pattern();

    Flux<String> process(RequestContext ctx, String text, Matcher matcher);
}
