package com.github.delegacy.youngbot.slack;

import static java.util.Objects.requireNonNull;

import com.github.delegacy.youngbot.event.EventService;
import com.slack.api.model.Message;

import reactor.core.publisher.Mono;

/**
 * TBW.
 */
public class SlackService {
    private final EventService eventService;

    private final SlackClient slackClient;

    /**
     * TBW.
     */
    public SlackService(EventService eventService, SlackClient slackClient) {
        this.eventService = requireNonNull(eventService, "eventService");
        this.slackClient = requireNonNull(slackClient, "slackClient");
    }

    /**
     * TBW.
     */
    public Mono<Void> processEvent(SlackEvent event) {
        final var flux = eventService.process(event);

        if (!(event instanceof SlackReplyableEvent)) {
            return flux.then();
        }

        final var cast = (SlackReplyableEvent) event;
        return flux.map(SlackEventResponse::of)
                   .flatMap(res -> reply(cast, res))
                   .then();
    }

    private Mono<String> reply(SlackReplyableEvent event, SlackEventResponse res) {
        if (res.ephemeral()) {
            return slackClient.postEphemeral(event.channel(), res.text(), event.user(), event.ts());
        }

        return slackClient.postMessage(event.channel(), res.text(), event.ts())
                          .map(Message::getTs);
    }
}
