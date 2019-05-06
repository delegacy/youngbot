package com.github.delegacy.youngbot.server.line;

import static com.github.delegacy.youngbot.server.util.JacksonUtils.serialize;
import static com.google.common.base.Preconditions.checkArgument;

import javax.inject.Inject;

import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.github.delegacy.youngbot.server.TheVoid;
import com.github.delegacy.youngbot.server.platform.Platform;
import com.github.delegacy.youngbot.server.platform.PlatformRpcException;
import com.github.delegacy.youngbot.server.platform.PlatformService;

import com.linecorp.armeria.client.HttpClient;
import com.linecorp.armeria.common.AggregatedHttpMessage;
import com.linecorp.armeria.common.HttpData;
import com.linecorp.armeria.common.HttpHeaderNames;
import com.linecorp.armeria.common.HttpHeaders;
import com.linecorp.armeria.common.HttpMethod;
import com.linecorp.armeria.common.HttpStatus;
import com.linecorp.armeria.common.MediaType;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.message.TextMessage;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class LineService implements PlatformService {
    private final HttpClient lineClient;
    private final String authorization;

    @Inject
    public LineService(@Value("${youngbot.line.client.base-uri}") String apiBaseUri,
                       @Value("${youngbot.line.channel-token}") String channelToken) {

        checkArgument(Strings.isNotEmpty(apiBaseUri), "empty apiBaseUri");
        checkArgument(Strings.isNotEmpty(channelToken), "empty channelToken");

        lineClient = HttpClient.of(apiBaseUri);
        authorization = "Bearer " + channelToken;
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

        final AggregatedHttpMessage request =
                AggregatedHttpMessage.of(HttpHeaders.of(HttpMethod.POST, "/v2/bot/message/reply")
                                                    .set(HttpHeaderNames.AUTHORIZATION, authorization)
                                                    .setObject(HttpHeaderNames.CONTENT_TYPE,
                                                               MediaType.JSON_UTF_8),
                                         HttpData.ofUtf8(serialize(replyMessage)));

        return Mono.fromFuture(lineClient.execute(request).aggregate())
                   .map(aggregated -> {
                       if (aggregated.status() == HttpStatus.OK) {
                           return TheVoid.INSTANCE;
                       } else {
                           throw new PlatformRpcException("Failed to reply with replyToken: " +
                                                          aggregated.status());
                       }
                   })
                   .doOnNext(ignored -> log.info("Replied with replyToken<{}>", replyToken))
                   .doOnError(t -> log.warn("Failed to reply with replyToken<{}>", replyToken, t));
    }
}
