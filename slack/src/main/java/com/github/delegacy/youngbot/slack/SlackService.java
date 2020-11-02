package com.github.delegacy.youngbot.slack;

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
        this.eventService = eventService;
        this.slackClient = slackClient;
    }

    /**
     * TBW.
     */
    public Mono<Void> processEvent(SlackEvent event) {
        if (!(event instanceof SlackReplyableEvent)) {
            return eventService.process(event).then();
        }

        return eventService.process(event)
                           .map(SlackEventResponse::of)
                           .flatMap(res -> reply((SlackReplyableEvent) event, res))
                           .then();
    }

    private Mono<String> reply(SlackReplyableEvent event, SlackEventResponse res) {
        if (res.secret()) {
            return slackClient.postEphemeral(event.channel(), res.text(), event.user(), event.ts());
        }

        return slackClient.postMessage(event.channel(), res.text(), event.ts())
                          .map(Message::getTs);
    }
}
