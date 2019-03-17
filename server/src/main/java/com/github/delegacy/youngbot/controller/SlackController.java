package com.github.delegacy.youngbot.controller;

import java.io.IOException;

import org.springframework.http.RequestEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.delegacy.youngbot.service.SlackService;
import com.hubspot.slack.client.jackson.ObjectMapperUtils;
import com.hubspot.slack.client.models.events.ChallengeEvent;
import com.hubspot.slack.client.models.events.ChallengeEventIF;
import com.hubspot.slack.client.models.events.ChallengeResponse;
import com.hubspot.slack.client.models.events.ChallengeResponseIF;
import com.hubspot.slack.client.models.events.SlackEvent;
import com.hubspot.slack.client.models.events.SlackEventMessage;
import com.hubspot.slack.client.models.events.SlackEventWrapper;
import com.hubspot.slack.client.models.events.SlackEventWrapperIF;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/slack/v1")
public class SlackController {
    private static Object deserialize(String str) {
        try {
            return ObjectMapperUtils.mapper().readValue(str, SlackEventWrapper.class);
        } catch (IOException e) {
            try {
                return ObjectMapperUtils.mapper().readValue(str, ChallengeEvent.class);
            } catch (IOException ignored) {
                // do nothing
            }

            log.warn("Failed to deserialize the str<{}>", str);
            throw new IllegalArgumentException("Failed to deserialize the str");
        }
    }

    private static String serialize(Object obj) {
        try {
            return ObjectMapperUtils.mapper().writeValueAsString(obj);
        } catch (IOException e) {
            log.warn("Failed to serialize the obj<{}>", obj, e);
            throw new IllegalArgumentException("Failed to serialize the obj");
        }
    }

    private final SlackService slackService;

    @PostMapping("/event")
    public Mono<String> onEvent(RequestEntity<String> req) {
        final String reqBody = req.getBody();
        log.debug("Received Slack event;reqBody<{}>", reqBody);

        final Object deserialized = deserialize(reqBody);
        if (deserialized instanceof SlackEventWrapper) {
            @SuppressWarnings("rawtypes")
            final SlackEventWrapperIF slackEventWrapper = (SlackEventWrapper) deserialized;
            log.debug("Received slackEventWrapper<{}>", slackEventWrapper);

            final SlackEvent slackEvent = slackEventWrapper.getEvent();
            log.debug("Received slackEvent<{}>", slackEvent);
            switch (slackEvent.getType()) {
                case MESSAGE:
                    final SlackEventMessage slackEventMessage = (SlackEventMessage) slackEvent;
                    log.debug("Received slackEventMessage<{}>", slackEventMessage);

                    slackService.postMessage(slackEventMessage.getChannelId(), slackEventMessage.getText());
                    break;
                default:
                    // do nothing
            }
            return Mono.just("");
        }

        if (deserialized instanceof ChallengeEventIF) {
            final ChallengeEventIF challengeEvent = (ChallengeEventIF) deserialized;
            final ChallengeResponseIF challengeResponse =
                    ChallengeResponse.builder()
                                     .setChallenge(challengeEvent.getChallenge())
                                     .build();
            return Mono.just(serialize(challengeResponse));
        }

        log.warn("Unsupported Slack event<{}>", deserialized);
        return Mono.just("");
    }
}
