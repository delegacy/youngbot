package com.github.delegacy.youngbot.server.slack;

import static com.google.common.base.MoreObjects.firstNonNull;
import static java.util.Objects.requireNonNull;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.github.delegacy.youngbot.server.message.service.MessageService;
import com.slack.api.app_backend.events.payload.EventsApiPayload;
import com.slack.api.bolt.App;
import com.slack.api.bolt.context.builtin.EventContext;
import com.slack.api.bolt.handler.BoltEventHandler;
import com.slack.api.bolt.request.Request;
import com.slack.api.bolt.response.Response;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import com.slack.api.model.event.MessageEvent;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * TBW.
 *
 * @link <a href="https://api.slack.com/events/message>message event</a>
 * @link <a href="https://api.slack.com/methods/chat.postMessage">chat.postMessage method</a>
 */
@Service
class SlackAppBlockingService {
    private static final Logger logger = LoggerFactory.getLogger(SlackAppBlockingService.class);

    private final App app;

    private final MessageService messageService;

    SlackAppBlockingService(App app, MessageService messageService) {
        this.app = requireNonNull(app, "app");
        this.messageService = requireNonNull(messageService, "messageService");

        app.event(MessageEvent.class, new MessageEventHandler());
    }

    @SuppressWarnings("rawtypes")
    Response run(Request request) throws Exception {
        return app.run(request);
    }

    class MessageEventHandler implements BoltEventHandler<MessageEvent> {
        @Override
        public Response apply(EventsApiPayload<MessageEvent> event, EventContext ctx)
                throws IOException, SlackApiException {

            final MessageEvent msgEvent = event.getEvent();
            logger.debug("Received text<{}> from channel<{}>", msgEvent.getText(), msgEvent.getChannel());

            final SlackMessageRequest msgReq =
                    new SlackMessageRequest(msgEvent.getText(), msgEvent.getChannel(),
                                            firstNonNull(msgEvent.getThreadTs(), msgEvent.getTs()));
            messageService.process(msgReq)
                          .flatMap(res -> Mono.fromCallable(
                                  () -> ctx.say(b -> b.channel(msgReq.channel())
                                                      .threadTs(msgReq.thread())
                                                      .text(res.text()))))
                          .subscribeOn(Schedulers.boundedElastic())
                          .subscribe(res -> onNextChatPostMessageResponse(msgReq, res),
                                     t -> logger.error("Failed to handle event<{}>;ctx<{}>", event, ctx, t));

            return ctx.ack();
        }

        private void onNextChatPostMessageResponse(SlackMessageRequest msgReq, ChatPostMessageResponse res) {
            if (res.isOk()) {
                if (res.getWarning() != null) {
                    logger.warn("Replied to text<{}> in channel<{}>;msg<{}>,warn<{}>",
                                msgReq.text(), msgReq.channel(), res.getMessage(), res.getWarning());
                } else {
                    logger.debug("Replied to text<{}> in channel<{}>;msg<{}>",
                                 msgReq.text(), msgReq.channel(), res.getMessage());
                }
            } else {
                logger.error("Failed to reply to text<{}> in channel<{}>;error<{}>",
                             msgReq.text(), msgReq.channel(), res.getError());
            }
        }
    }
}
