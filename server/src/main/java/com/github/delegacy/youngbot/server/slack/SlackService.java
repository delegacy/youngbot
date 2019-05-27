package com.github.delegacy.youngbot.server.slack;

import static com.github.delegacy.youngbot.server.slack.SlackJacksonUtils.deserializeResponse;
import static com.github.delegacy.youngbot.server.slack.SlackJacksonUtils.serialize;
import static com.google.common.base.Preconditions.checkArgument;

import javax.inject.Inject;

import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.github.delegacy.youngbot.server.TheVoid;
import com.github.delegacy.youngbot.server.platform.Platform;
import com.github.delegacy.youngbot.server.platform.PlatformRpcException;
import com.github.delegacy.youngbot.server.platform.PlatformService;
import com.hubspot.slack.client.methods.params.chat.ChatPostMessageParams;
import com.hubspot.slack.client.models.response.chat.ChatPostMessageResponse;

import com.linecorp.armeria.client.HttpClient;
import com.linecorp.armeria.client.HttpClientBuilder;
import com.linecorp.armeria.common.HttpHeaderNames;
import com.linecorp.armeria.common.HttpMethod;
import com.linecorp.armeria.common.HttpRequest;
import com.linecorp.armeria.common.MediaType;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class SlackService implements PlatformService {
    private final HttpClient slackClient;

    @Inject
    public SlackService(@Value("${youngbot.slack.client.base-uri}") String slackClientBaseUri,
                        @Value("${youngbot.slack.client.bot-token}") String botToken) {

        checkArgument(Strings.isNotEmpty(slackClientBaseUri), "empty slackClientBaseUri");
        checkArgument(Strings.isNotEmpty(botToken), "empty botToken");

        slackClient = new HttpClientBuilder(slackClientBaseUri)
                .addHttpHeader(HttpHeaderNames.AUTHORIZATION, "Bearer " + botToken)
                .build();
    }

    @Override
    public Platform platform() {
        return Platform.SLACK;
    }

    @Override
    public Mono<TheVoid> replyMessage(String channel, String text) {
        final ChatPostMessageParams chatPostMessageParams =
                ChatPostMessageParams.builder()
                                     .setChannelId(channel)
                                     .setText(text)
                                     .build();

        final HttpRequest request = HttpRequest.of(HttpMethod.POST, "/chat.postMessage",
                                                   MediaType.JSON_UTF_8, serialize(chatPostMessageParams));

        return Mono.fromFuture(slackClient.execute(request).aggregate())
                   .map(aggregated -> deserializeResponse(aggregated.content().toStringUtf8(),
                                                          ChatPostMessageResponse.class))
                   .map(response -> {
                       if (response.isOk()) {
                           return TheVoid.INSTANCE;
                       } else {
                           throw new PlatformRpcException("Failed to reply to channel: " + response);
                       }
                   })
                   .doOnNext(ignored -> log.info("Replied to channel<{}>", channel))
                   .doOnError(t -> log.warn("Failed to reply to channel<{}>", channel, t));
    }
}
