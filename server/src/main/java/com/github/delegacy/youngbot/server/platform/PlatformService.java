package com.github.delegacy.youngbot.server.platform;

import com.github.delegacy.youngbot.server.TheVoid;

import reactor.core.publisher.Mono;

public interface PlatformService {
    Platform platform();

    Mono<TheVoid> replyMessage(String to, String text);
}
