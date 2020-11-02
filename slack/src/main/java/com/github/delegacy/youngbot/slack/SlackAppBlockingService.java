package com.github.delegacy.youngbot.slack;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.slack.api.app_backend.events.payload.EventsApiPayload;
import com.slack.api.bolt.App;
import com.slack.api.bolt.context.builtin.EventContext;
import com.slack.api.bolt.handler.BoltEventHandler;
import com.slack.api.bolt.request.Request;
import com.slack.api.bolt.response.Response;
import com.slack.api.methods.SlackApiException;
import com.slack.api.model.event.MessageEvent;
import com.slack.api.model.event.ReactionAddedEvent;

/**
 * TBW.
 *
 * @see <a href="https://api.slack.com/events/message">message event</a>
 * @see <a href="https://api.slack.com/methods/chat.postMessage">chat.postMessage method</a>
 */
class SlackAppBlockingService {
    private static final Logger logger = LoggerFactory.getLogger(SlackAppBlockingService.class);

    private final App app;

    private final SlackService slackService;

    /**
     * TBW.
     */
    SlackAppBlockingService(App app, SlackService slackService) {
        this.app = app;
        this.slackService = slackService;
    }

    void initialize() {
        app.event(MessageEvent.class, new MessageEventHandler());
        app.event(ReactionAddedEvent.class, new ReactionAddedEventHandler());
    }

    @SuppressWarnings("rawtypes")
    Response run(Request request) throws Exception {
        return app.run(request);
    }

    class MessageEventHandler implements BoltEventHandler<MessageEvent> {
        @Override
        public Response apply(EventsApiPayload<MessageEvent> payload, EventContext ctx)
                throws IOException, SlackApiException {
            final MessageEvent event = payload.getEvent();
            logger.debug("Received text<{}> from channel<{}>", event.getText(), event.getChannel());

            slackService.processEvent(SlackMessageEvent.of(event))
                        .subscribe(null,
                                   t -> logger.error("Failed to process event<{}>", event, t));

            return ctx.ack();
        }
    }

    class ReactionAddedEventHandler implements BoltEventHandler<ReactionAddedEvent> {
        @Override
        public Response apply(EventsApiPayload<ReactionAddedEvent> payload, EventContext ctx)
                throws IOException, SlackApiException {
            final ReactionAddedEvent event = payload.getEvent();
            logger.debug("Received reaction<{}> from channel<{}>",
                         event.getReaction(), event.getItem().getChannel());

            slackService.processEvent(SlackReactionEvent.of(event))
                        .subscribe(null,
                                   t -> logger.error("Failed to process event<{}>", event, t));

            return ctx.ack();
        }
    }
}
