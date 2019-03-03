package com.github.delegacy.youngbot.service;

import java.io.IOException;

import javax.inject.Inject;

import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.common.base.Preconditions;
import com.hubspot.slack.client.jackson.ObjectMapperUtils;
import com.hubspot.slack.client.methods.params.chat.ChatPostMessageParams;

import com.linecorp.armeria.client.HttpClient;
import com.linecorp.armeria.common.AggregatedHttpMessage;
import com.linecorp.armeria.common.HttpData;
import com.linecorp.armeria.common.HttpHeaderNames;
import com.linecorp.armeria.common.HttpHeaders;
import com.linecorp.armeria.common.HttpMethod;
import com.linecorp.armeria.common.MediaType;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class SlackService {
    private static String serialize(Object obj) {
        try {
            return ObjectMapperUtils.mapper().writeValueAsString(obj);
        } catch (IOException e) {
            log.warn("Failed to serialize the obj<{}>", obj, e);
            throw new IllegalArgumentException("Failed to serialize the obj");
        }
    }

    private final HttpClient slackClient;
    private final String authorization;

    @Inject
    public SlackService(@Value("${youngbot.slack.client.base-uri}") String slackClientBaseUri,
                        @Value("${youngbot.slack.client.bot-token}") String botToken) {

        Preconditions.checkArgument(Strings.isNotEmpty(slackClientBaseUri), "empty slackClientBaseUri");
        Preconditions.checkArgument(Strings.isNotEmpty(botToken), "empty botToken");

        slackClient = HttpClient.of(slackClientBaseUri);
        authorization = "Bearer " + botToken;
    }

    public void postMessage(String channel, String text) {
        final ChatPostMessageParams chatPostMessageParams =
                ChatPostMessageParams.builder()
                                     .setChannelId(channel)
                                     .setText(text)
                                     .build();

        final AggregatedHttpMessage request =
                AggregatedHttpMessage.of(HttpHeaders.of(HttpMethod.POST, "/chat.postMessage")
                                                    .set(HttpHeaderNames.AUTHORIZATION, authorization)
                                                    .setObject(HttpHeaderNames.CONTENT_TYPE, MediaType.JSON_UTF_8),
                                         HttpData.ofUtf8(serialize(chatPostMessageParams)));

        slackClient.execute(request).aggregate()
                   .whenComplete((res, cause) -> {
                       if (cause != null) {
                           log.warn("Failed to send a message to channel<{}>", channel, cause);
                       } else {
                           log.info("Sent a message to channel<{}>;res<{}>",
                                    channel, res.content().toStringUtf8());
                       }
                   });
    }
}
