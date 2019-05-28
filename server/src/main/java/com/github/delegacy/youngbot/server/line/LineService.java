package com.github.delegacy.youngbot.server.line;

import static com.github.delegacy.youngbot.server.util.JacksonUtils.serialize;
import static com.google.common.base.Preconditions.checkArgument;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.github.delegacy.youngbot.server.TheVoid;
import com.github.delegacy.youngbot.server.platform.Platform;
import com.github.delegacy.youngbot.server.platform.PlatformRpcException;
import com.github.delegacy.youngbot.server.platform.PlatformService;

import com.linecorp.armeria.client.HttpClient;
import com.linecorp.armeria.client.HttpClientBuilder;
import com.linecorp.armeria.common.HttpHeaderNames;
import com.linecorp.armeria.common.HttpMethod;
import com.linecorp.armeria.common.HttpRequest;
import com.linecorp.armeria.common.HttpStatus;
import com.linecorp.armeria.common.MediaType;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.message.TextMessage;

import reactor.core.publisher.Mono;

@Service
public class LineService implements PlatformService {
    private static final Logger logger = LoggerFactory.getLogger(LineService.class);

    private final HttpClient lineClient;

    public LineService(@Value("${youngbot.line.client.base-uri}") String apiBaseUri,
                       @Value("${youngbot.line.channel-token}") String channelToken) {

        checkArgument(StringUtils.isNotEmpty(apiBaseUri), "empty apiBaseUri");
        checkArgument(StringUtils.isNotEmpty(channelToken), "empty channelToken");

        lineClient = new HttpClientBuilder(apiBaseUri)
                .addHttpHeader(HttpHeaderNames.AUTHORIZATION, "Bearer " + channelToken)
                .build();
    }

    @Override
    public Platform platform() {
        return Platform.LINE;
    }

    @Override
    public Mono<TheVoid> replyMessage(String replyToken, String text) {
        final TextMessage textMessage = TextMessage.builder()
                                                   .text(text)
                                                   .build();

        final ReplyMessage replyMessage = new ReplyMessage(replyToken, textMessage);

        final HttpRequest request = HttpRequest.of(HttpMethod.POST, "/v2/bot/message/reply",
                                                   MediaType.JSON_UTF_8, serialize(replyMessage));

        return Mono.fromFuture(lineClient.execute(request).aggregate())
                   .map(aggregated -> {
                       if (aggregated.status() == HttpStatus.OK) {
                           return TheVoid.INSTANCE;
                       } else {
                           throw new PlatformRpcException("Failed to reply with replyToken: " +
                                                          aggregated.status());
                       }
                   })
                   .doOnNext(ignored -> logger.info("Replied with replyToken<{}>", replyToken))
                   .doOnError(t -> logger.warn("Failed to reply with replyToken<{}>", replyToken, t));
    }
}
