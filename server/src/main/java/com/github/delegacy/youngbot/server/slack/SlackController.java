package com.github.delegacy.youngbot.server.slack;

import static com.github.delegacy.youngbot.server.slack.SlackJacksonUtils.deserializeEvent;
import static com.github.delegacy.youngbot.server.slack.SlackJacksonUtils.serialize;
import static java.util.Objects.requireNonNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.RequestEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

import com.github.delegacy.youngbot.server.RequestContext;
import com.github.delegacy.youngbot.server.message.service.MessageService;
import com.github.delegacy.youngbot.server.platform.Platform;
import com.hubspot.slack.client.models.events.ChallengeEventIF;
import com.hubspot.slack.client.models.events.ChallengeResponse;
import com.hubspot.slack.client.models.events.SlackEvent;
import com.hubspot.slack.client.models.events.SlackEventMessage;
import com.hubspot.slack.client.models.events.SlackEventWrapperIF;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/slack/v1")
public class SlackController {
    private static final Logger logger = LoggerFactory.getLogger(SlackController.class);

    private final MessageService messageService;

    public SlackController(MessageService messageService) {
        this.messageService = requireNonNull(messageService, "messageService");
    }

    @PostMapping("/event")
    public Mono<String> onEvent(RequestEntity<String> req, ServerWebExchange exchange) {
        final String reqBody = req.getBody();
        logger.debug("Received a Slack event;reqBody<{}>", reqBody);

        final Object deserialized = deserializeEvent(reqBody);
        if (deserialized instanceof SlackEventWrapperIF) {
            @SuppressWarnings("rawtypes")
            final SlackEventWrapperIF slackEventWrapper = (SlackEventWrapperIF) deserialized;
            final SlackEvent slackEvent = slackEventWrapper.getEvent();
            if (slackEvent instanceof SlackEventMessage) {
                final SlackEventMessage slackEventMessage = (SlackEventMessage) slackEvent;

                final RequestContext ctx = new RequestContext(Platform.SLACK, exchange,
                                                              slackEventMessage.getText(),
                                                              slackEventMessage.getChannelId());

                messageService.process(ctx, slackEventMessage.getText());
                logger.info("Processed a SlackEventMessage;ctx<{}>", ctx);
            }

            return Mono.just("");
        }

        if (deserialized instanceof ChallengeEventIF) {
            final ChallengeEventIF challengeEvent = (ChallengeEventIF) deserialized;
            return Mono.just(serialize(ChallengeResponse.builder()
                                                        .setChallenge(challengeEvent.getChallenge())
                                                        .build()));
        }

        return Mono.just("");
    }
}
