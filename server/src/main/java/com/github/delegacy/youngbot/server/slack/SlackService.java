package com.github.delegacy.youngbot.server.slack;

import static com.github.delegacy.youngbot.server.slack.SlackJacksonUtils.deserializeResponse;
import static com.github.delegacy.youngbot.server.slack.SlackJacksonUtils.serialize;
import static com.google.common.base.Preconditions.checkArgument;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.github.delegacy.youngbot.server.message.MessageContext;
import com.github.delegacy.youngbot.server.TheVoid;
import com.github.delegacy.youngbot.server.platform.Platform;
import com.github.delegacy.youngbot.server.platform.PlatformRpcException;
import com.github.delegacy.youngbot.server.platform.PlatformService;
import com.hubspot.slack.client.methods.params.chat.ChatPostMessageParams;
import com.hubspot.slack.client.models.response.chat.ChatPostMessageResponse;

import com.linecorp.armeria.client.logging.LoggingClient;
import com.linecorp.armeria.client.metric.MetricCollectingClient;
import com.linecorp.armeria.client.WebClient;
import com.linecorp.armeria.common.HttpHeaderNames;
import com.linecorp.armeria.common.HttpMethod;
import com.linecorp.armeria.common.HttpRequest;
import com.linecorp.armeria.common.MediaType;
import com.linecorp.armeria.common.metric.MeterIdPrefixFunction;

import reactor.core.publisher.Mono;

@Service
public class SlackService implements PlatformService {
    private static final Logger logger = LoggerFactory.getLogger(SlackService.class);

    private final WebClient slackClient;

    public SlackService(@Value("${youngbot.slack.client.base-uri}") String slackClientBaseUri,
                        @Value("${youngbot.slack.client.bot-token}") String botToken) {

        checkArgument(StringUtils.isNotEmpty(slackClientBaseUri), "empty slackClientBaseUri");
        checkArgument(StringUtils.isNotEmpty(botToken), "empty botToken");

        slackClient = WebClient.builder(slackClientBaseUri)
                .addHeader(HttpHeaderNames.AUTHORIZATION, "Bearer " + botToken)
                .decorator(LoggingClient.newDecorator())
                .decorator(MetricCollectingClient.newDecorator(
                        MeterIdPrefixFunction.ofDefault("armeria.client").withTags("service", "slack")))
                .build();
    }

    @Override
    public Platform platform() {
        return Platform.SLACK;
    }

    @Override
    public Mono<TheVoid> replyMessage(MessageContext msgCtx, String text) {
        checkArgument(msgCtx instanceof SlackMessageContext, "incompatible msgCtx");

        final String channelId = msgCtx.channelId();

        final ChatPostMessageParams chatPostMessageParams =
                ChatPostMessageParams.builder()
                                     .setChannelId(channelId)
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
                   .doOnNext(ignored -> logger.info("Replied to channelId<{}>", channelId))
                   .doOnError(t -> logger.warn("Failed to reply to channelId<{}>", channelId, t));
    }
}
