package com.github.delegacy.youngbot.server.slack;

import static com.github.delegacy.youngbot.server.ReactorContextFilter.REACTOR_CONTEXT_KEY;
import static com.github.delegacy.youngbot.server.slack.SlackJacksonUtils.deserializeEvent;
import static java.util.Objects.requireNonNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

import com.github.delegacy.youngbot.server.message.MessageContext;
import com.github.delegacy.youngbot.server.message.service.MessageService;
import com.hubspot.slack.client.models.events.ChallengeEventIF;
import com.hubspot.slack.client.models.events.SlackEvent;
import com.hubspot.slack.client.models.events.SlackEventMessage;
import com.hubspot.slack.client.models.events.SlackEventWrapperIF;

import reactor.core.scheduler.Schedulers;
import reactor.util.context.Context;

/**
 * TBW.
 */
@RestController
@RequestMapping("/api/slack/v1")
public class SlackController {
    private static final Logger logger = LoggerFactory.getLogger(SlackController.class);

    private final MessageService messageService;

    /**
     * TBW.
     */
    public SlackController(MessageService messageService) {
        this.messageService = requireNonNull(messageService, "messageService");
    }

    /**
     * TBW.
     */
    @PostMapping("/event")
    public ResponseEntity<String> onEvent(RequestEntity<String> req, ServerWebExchange exchange) {
        final String reqBody = requireNonNull(req.getBody(), "reqBody");
        logger.debug("Received a Slack event;reqBody<{}>", reqBody);

        final Object deserialized = deserializeEvent(reqBody);
        if (deserialized instanceof SlackEventWrapperIF) {
            @SuppressWarnings("rawtypes")
            final SlackEventWrapperIF slackEventWrapper = (SlackEventWrapperIF) deserialized;
            final SlackEvent slackEvent = slackEventWrapper.getEvent();
            if (slackEvent instanceof SlackEventMessage) {
                final SlackEventMessage slackEventMessage = (SlackEventMessage) slackEvent;

                final MessageContext msgCtx = new SlackMessageContext(slackEventMessage.getText(),
                                                                      slackEventMessage.getChannelId());

                messageService.process(msgCtx)
                              .subscribeOn(Schedulers.elastic())
                              .subscriberContext((Context) exchange.getRequiredAttribute(REACTOR_CONTEXT_KEY))
                              .subscribe();

                logger.info("Processed a SlackEventMessage;msgCtx<{}>", msgCtx);
            }
        } else if (deserialized instanceof ChallengeEventIF) {
            final ChallengeEventIF challengeEvent = (ChallengeEventIF) deserialized;
            return ResponseEntity.ok().body(challengeEvent.getChallenge());
        }

        return ResponseEntity.ok().build();
    }
}
