package com.github.delegacy.youngbot.slack;

import static java.util.Objects.requireNonNull;

import java.io.Closeable;
import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.websocket.CloseReason.CloseCodes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.delegacy.youngbot.message.MessageService;
import com.github.delegacy.youngbot.slack.reaction.SlackReactionRequest;
import com.github.delegacy.youngbot.slack.reaction.SlackReactionService;
import com.google.common.annotations.VisibleForTesting;
import com.slack.api.model.event.Event;
import com.slack.api.model.event.GoodbyeEvent;
import com.slack.api.model.event.HelloEvent;
import com.slack.api.model.event.MessageEvent;
import com.slack.api.model.event.ReactionAddedEvent;
import com.slack.api.rtm.RTMClient;
import com.slack.api.rtm.RTMEventHandler;
import com.slack.api.rtm.RTMEventsDispatcher;
import com.slack.api.rtm.RTMEventsDispatcherFactory;
import com.slack.api.rtm.message.PingMessage;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;

/**
 * TBW.
 */
public class SlackRtmService implements Closeable {
    private static final Logger logger = LoggerFactory.getLogger(SlackRtmService.class);

    private final AtomicLong msgId = new AtomicLong();

    private final ScheduledExecutorService executorService;

    private final RTMClient rtmClient;

    private final MessageService messageService;

    private final SlackReactionService slackReactionService;

    private final SlackClient slackClient;

    private final RTMEventsDispatcher rtmEventDispatcher;

    // Respect the rate limit for reconnect, https://api.slack.com/docs/rate-limits#rtm
    private final RateLimiter rateLimiterReconnect =
            RateLimiter.of("reconnect",
                           RateLimiterConfig.custom()
                                            .limitForPeriod(1)
                                            .limitRefreshPeriod(Duration.ofMinutes(1))
                                            .build());

    private volatile boolean sessionClosed = true;

    @Nullable
    private ScheduledFuture<?> pingTaskFuture;

    /**
     * TBW.
     */
    public SlackRtmService(RTMClient rtmClient, MessageService messageService, SlackClient slackClient,
                           SlackReactionService slackReactionService) {
        this(rtmClient, messageService, slackClient, slackReactionService,
             Executors.newSingleThreadScheduledExecutor(), RTMEventsDispatcherFactory.getInstance());
    }

    @VisibleForTesting
    SlackRtmService(RTMClient rtmClient, MessageService messageService, SlackClient slackClient,
                    SlackReactionService slackReactionService,
                    ScheduledExecutorService executorService, RTMEventsDispatcher rtmEventDispatcher) {
        this.rtmClient = requireNonNull(rtmClient, "rtmClient");
        this.messageService = requireNonNull(messageService, "messageService");
        this.slackClient = requireNonNull(slackClient, "slackClient");
        this.slackReactionService = requireNonNull(slackReactionService, "slackReactionService");
        this.executorService = requireNonNull(executorService, "executorService");
        this.rtmEventDispatcher = requireNonNull(rtmEventDispatcher, "rtmEventDispatcher");
    }

    /**
     * TBW.
     *
     * @throws Exception TBW
     */
    @PostConstruct
    public void initialize() throws Exception {
        configureRtmClient();
    }

    private void configureRtmClient() throws Exception {
        rtmEventDispatcher.register(new HelloEventHandler());
        rtmEventDispatcher.register(new GoodbyeEventHandler());
        rtmEventDispatcher.register(new MessageEventHandler());
        rtmEventDispatcher.register(new ReactionAddedEventHandler());

        rtmClient.addMessageHandler(rtmEventDispatcher.toMessageHandler());
        rtmClient.addErrorHandler(t -> logger.warn("A RTM session error occurred.", t));
        rtmClient.addCloseHandler(reason -> {
            if (reason.getCloseCode() == CloseCodes.NORMAL_CLOSURE) {
                logger.info("The RTM session is closed because of {}.", reason);
            } else {
                logger.error("The RTM session is closed because of {}.", reason);
            }

            sessionClosed = true;
        });

        reconnect();
    }

    @VisibleForTesting
    boolean isSessionClosed() {
        return sessionClosed;
    }

    @VisibleForTesting
    void setSessionClosed(boolean sessionClosed) {
        this.sessionClosed = sessionClosed;
    }

    @VisibleForTesting
    @Nullable
    ScheduledFuture<?> getPingTaskFuture() {
        return pingTaskFuture;
    }

    @VisibleForTesting
    void setPingTaskFuture(@Nullable ScheduledFuture<?> pingTaskFuture) {
        this.pingTaskFuture = pingTaskFuture;
    }

    @Override
    public void close() throws IOException {
        executorService.shutdown();

        rtmClient.close();
    }

    /**
     * TBW.
     */
    public void registerRtmEventHandler(RTMEventHandler<? extends Event> rtmEventHandler) {
        rtmEventDispatcher.register(rtmEventHandler);
    }

    /**
     * TBW.
     */
    public void deregisterRtmEventHandler(RTMEventHandler<? extends Event> rtmEventHandler) {
        rtmEventDispatcher.deregister(rtmEventHandler);
    }

    private void reconnect() {
        if (!rateLimiterReconnect.acquirePermission()) {
            logger.warn("Failed to acquire permission from the rate limiter");
            return;
        }

        try {
            rtmClient.reconnect();
        } catch (Exception e) {
            logger.warn("Failed to reconnect to Slack", e);
        }
    }

    class HelloEventHandler extends RTMEventHandler<HelloEvent> {
        @Override
        public void handle(HelloEvent event) {
            sessionClosed = false;

            if (pingTaskFuture == null) {
                pingTaskFuture = executorService.scheduleWithFixedDelay(
                        new PingTask(), 1L, 30L, TimeUnit.SECONDS);
            }
        }
    }

    class PingTask implements Runnable {
        @Override
        public void run() {
            if (sessionClosed) {
                reconnect();
            } else {
                try {
                    rtmClient.sendMessage(PingMessage.builder()
                                                     .id(msgId.incrementAndGet())
                                                     .build()
                                                     .toJSONString());
                } catch (RuntimeException e) {
                    logger.warn("Failed to ping Slack", e);
                }
            }
        }
    }

    class GoodbyeEventHandler extends RTMEventHandler<GoodbyeEvent> {
        @Override
        public void handle(GoodbyeEvent event) {
            reconnect();
        }
    }

    class MessageEventHandler extends RTMEventHandler<MessageEvent> {
        @Override
        public void handle(MessageEvent event) {
            logger.debug("Received text<{}> from channel<{}>", event.getText(), event.getChannel());

            final SlackMessageRequest req = SlackMessageRequest.of(event);
            messageService.process(req)
                          .flatMap(res -> slackClient.sendMessage(req.channel(), res.text(), req.thread()))
                          .subscribe(null,
                                     t -> logger.error("Failed to handle event<{}>", event, t));
        }
    }

    class ReactionAddedEventHandler extends RTMEventHandler<ReactionAddedEvent> {
        @Override
        public void handle(ReactionAddedEvent event) {
            logger.debug("Received reaction<{}> from channel<{}>",
                         event.getReaction(), event.getItem().getChannel());

            final SlackReactionRequest req = SlackReactionRequest.of(event);
            slackReactionService.process(req)
                          .flatMap(res -> slackClient.sendMessage(req.channel(), res.text(), req.messageTs()))
                          .subscribe(null,
                                     t -> logger.error("Failed to handle event<{}>", event, t));
        }
    }
}
