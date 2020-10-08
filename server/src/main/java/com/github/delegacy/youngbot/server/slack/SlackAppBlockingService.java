package com.github.delegacy.youngbot.server.slack;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.util.regex.Matcher;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.github.delegacy.youngbot.server.message.MessageContext;
import com.github.delegacy.youngbot.server.message.handler.MessageHandlerManager;
import com.slack.api.app_backend.events.payload.EventsApiPayload;
import com.slack.api.bolt.App;
import com.slack.api.bolt.context.builtin.EventContext;
import com.slack.api.bolt.handler.BoltEventHandler;
import com.slack.api.bolt.request.Request;
import com.slack.api.bolt.response.Response;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import com.slack.api.model.event.MessageEvent;

import reactor.core.publisher.Flux;
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

    private final MessageHandlerManager messageHandlerManager;

    @Inject
    SlackAppBlockingService(App app, MessageHandlerManager messageHandlerManager) {
        this.app = requireNonNull(app, "app");
        this.messageHandlerManager = requireNonNull(messageHandlerManager, "messageHandlerManager");

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

            final MessageContext msgCtx = new SlackMessageContext(msgEvent.getText(), msgEvent.getChannel());

            Flux.fromIterable(messageHandlerManager.handlers())
                .concatMap(handler -> {
                    final Matcher matcher = handler.pattern().matcher(msgCtx.text());
                    if (!matcher.matches()) {
                        return Flux.empty();
                    }
                    return handler.handle(msgCtx, matcher);
                })
                .filter(s -> !s.isEmpty())
                .flatMap(s -> Mono.fromCallable(
                        () -> ctx.say(b -> b.channel(ctx.getChannelId())
                                            .threadTs(msgEvent.getThreadTs() == null ? msgEvent.getTs()
                                                                                     : msgEvent.getThreadTs())
                                            .text(s))))
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe(res -> onNextChatPostMessageResponse(msgEvent, res),
                           t -> logger.error("Failed to handle event<{}>;ctx<{}>", event, ctx, t),
                           () -> logger.info("Replied to text<{}> in channel<{}>",
                                             msgEvent.getText(), msgEvent.getChannel()));

            return ctx.ack();
        }

        private void onNextChatPostMessageResponse(MessageEvent event, ChatPostMessageResponse res) {
            if (res.isOk()) {
                if (res.getWarning() != null) {
                    logger.warn("Replied to text<{}> in channel<{}>;msg<{}>,warn<{}>",
                                event.getText(), event.getChannel(), res.getMessage(), res.getWarning());
                } else {
                    logger.debug("Replied to text<{}> in channel<{}>;msg<{}>",
                                 event.getText(), event.getChannel(), res.getMessage());
                }
            } else {
                logger.error("Failed to reply to text<{}> in channel<{}>;error<{}>",
                             event.getText(), event.getChannel(), res.getError());
            }
        }
    }
}
