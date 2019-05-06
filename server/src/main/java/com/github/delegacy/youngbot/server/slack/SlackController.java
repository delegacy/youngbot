package com.github.delegacy.youngbot.server.slack;

import static com.github.delegacy.youngbot.server.slack.SlackJacksonUtils.deserializeEvent;
import static com.github.delegacy.youngbot.server.slack.SlackJacksonUtils.serialize;

import org.springframework.http.RequestEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.delegacy.youngbot.server.RequestContext;
import com.github.delegacy.youngbot.server.message.service.MessageService;
import com.github.delegacy.youngbot.server.platform.Platform;
import com.hubspot.slack.client.models.events.ChallengeEventIF;
import com.hubspot.slack.client.models.events.ChallengeResponse;
import com.hubspot.slack.client.models.events.SlackEvent;
import com.hubspot.slack.client.models.events.SlackEventMessage;
import com.hubspot.slack.client.models.events.SlackEventWrapperIF;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/slack/v1")
public class SlackController {
    private final MessageService messageService;

    @PostMapping("/event")
    public Mono<String> onEvent(RequestEntity<String> req) {
        final String reqBody = req.getBody();
        log.debug("Received Slack event;reqBody<{}>", reqBody);

        final Object deserialized = deserializeEvent(reqBody);
        if (deserialized instanceof SlackEventWrapperIF) {
            @SuppressWarnings("rawtypes")
            final SlackEventWrapperIF slackEventWrapper = (SlackEventWrapperIF) deserialized;
            final SlackEvent slackEvent = slackEventWrapper.getEvent();
            switch (slackEvent.getType()) {
                case MESSAGE:
                    if (slackEvent instanceof SlackEventMessage) {
                        final SlackEventMessage slackEventMessage = (SlackEventMessage) slackEvent;

                        final RequestContext ctx = new RequestContext();
                        ctx.platform(Platform.SLACK);
                        ctx.text(slackEventMessage.getText());
                        ctx.replyTo(slackEventMessage.getChannelId());

                        messageService.process(ctx, slackEventMessage.getText());
                    } else {
                        log.debug("Unsupported slackEvent<{}>", slackEvent);
                    }
                    break;
                default:
                    // do nothing
            }
            return Mono.just("");
        }

        if (deserialized instanceof ChallengeEventIF) {
            final ChallengeEventIF challengeEvent = (ChallengeEventIF) deserialized;
            return Mono.just(serialize(ChallengeResponse.builder()
                                                        .setChallenge(challengeEvent.getChallenge())
                                                        .build()));
        }

        log.warn("Unsupported slackEvent<{}>", deserialized);
        return Mono.just("");
    }
}
