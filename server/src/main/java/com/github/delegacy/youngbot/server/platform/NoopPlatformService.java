package com.github.delegacy.youngbot.server.platform;

import com.github.delegacy.youngbot.server.message.MessageContext;
import com.github.delegacy.youngbot.server.TheVoid;

import reactor.core.publisher.Mono;

class NoopPlatformService implements PlatformService {
    @Override
    public Platform platform() {
        return Platform.UNKNOWN;
    }

    @Override
    public Mono<TheVoid> replyMessage(MessageContext msgCtx, String text) {
        return Mono.empty();
    }
}
