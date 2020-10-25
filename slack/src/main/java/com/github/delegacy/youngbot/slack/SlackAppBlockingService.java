package com.github.delegacy.youngbot.slack;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.delegacy.youngbot.message.MessageService;
import com.github.delegacy.youngbot.slack.reaction.SlackReactionRequest;
import com.github.delegacy.youngbot.slack.reaction.SlackReactionService;
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

    private final MessageService messageService;

    private final SlackClient slackClient;

    private final SlackReactionService slackReactionService;

    /**
     * TBW.
     */
    SlackAppBlockingService(App app, MessageService messageService, SlackClient slackClient,
                            SlackReactionService slackReactionService) {
        this.app = app;
        this.messageService = messageService;
        this.slackClient = slackClient;
        this.slackReactionService = slackReactionService;
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
        public Response apply(EventsApiPayload<MessageEvent> eventPayload, EventContext ctx)
                throws IOException, SlackApiException {

            final MessageEvent event = eventPayload.getEvent();
            logger.debug("Received text<{}> from channel<{}>", event.getText(), event.getChannel());

            final SlackMessageRequest req = SlackMessageRequest.of(event);
            messageService.process(req)
                          .flatMap(res -> slackClient.sendMessage(req.channel(), res.text(), req.threadTs()))
                          .subscribe(null,
                                     t -> logger.error("Failed to handle event<{}>;ctx<{}>",
                                                       eventPayload, ctx, t));

            return ctx.ack();
        }
    }

    class ReactionAddedEventHandler implements BoltEventHandler<ReactionAddedEvent> {
        @Override
        public Response apply(EventsApiPayload<ReactionAddedEvent> eventPayLoad, EventContext ctx)
                throws IOException, SlackApiException {

            final ReactionAddedEvent event = eventPayLoad.getEvent();
            logger.debug("Received reaction<{}> from channel<{}>",
                         event.getReaction(), event.getItem().getChannel());

            final SlackReactionRequest req = SlackReactionRequest.of(event);
            slackReactionService.process(req)
                                .flatMap(res -> slackClient.sendMessage(req.channel(), res.text(),
                                                                        req.messageTs()))
                                .subscribe(null,
                                           t -> logger.error("Failed to handle event<{}>", event, t));

            return ctx.ack();
        }
    }
}
