package com.github.delegacy.youngbot.server.platform;

import com.github.delegacy.youngbot.server.TheVoid;
import com.github.delegacy.youngbot.server.message.MessageContext;

import reactor.core.publisher.Mono;

/**
 * TBW.
 */
public interface PlatformService {
    /**
     * TBW.
     */
    Platform platform();

    /**
     * TBW.
     */
    Mono<TheVoid> replyMessage(MessageContext msgCtx, String text);
}
