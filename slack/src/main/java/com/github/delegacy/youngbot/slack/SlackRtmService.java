package com.github.delegacy.youngbot.slack;

import static java.util.Objects.requireNonNull;

import java.io.Closeable;
import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.PostConstruct;
import javax.websocket.CloseReason.CloseCodes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.delegacy.youngbot.Consumers;
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

    private final RTMClient rtmClient;

    private final SlackService slackService;

    private final ScheduledExecutorService executorService;

    private final RTMEventsDispatcher rtmEventDispatcher;

    private final AtomicLong rtmMessageId = new AtomicLong();

    // Respect the rate limit for reconnect, https://api.slack.com/docs/rate-limits#rtm
    private final RateLimiter rateLimiterReconnect =
            RateLimiter.of("reconnect",
                           RateLimiterConfig.custom()
                                            .limitForPeriod(1)
                                            .limitRefreshPeriod(Duration.ofMinutes(1))
                                            .build());

    /**
     * TBW.
     */
    public SlackRtmService(RTMClient rtmClient, SlackService slackService) {
        this(rtmClient, slackService,
             Executors.newSingleThreadScheduledExecutor(), RTMEventsDispatcherFactory.getInstance());
    }

    @VisibleForTesting
    SlackRtmService(RTMClient rtmClient, SlackService slackService,
                    ScheduledExecutorService executorService, RTMEventsDispatcher rtmEventDispatcher) {
        this.rtmClient = requireNonNull(rtmClient, "rtmClient");
        this.slackService = requireNonNull(slackService, "slackService");
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

            if (!executorService.isShutdown()) {
                // Immediately ping for fast reconnection
                executorService.submit(new PingTask());
            }
        });

        reconnect();

        executorService.scheduleWithFixedDelay(
                new PingTask(), 30L, 30L, TimeUnit.SECONDS);
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

    static class HelloEventHandler extends RTMEventHandler<HelloEvent> {
        @Override
        public void handle(HelloEvent event) {
            logger.debug("Received hello");
        }
    }

    class PingTask implements Runnable {
        @Override
        public void run() {
            try {
                rtmClient.sendMessage(PingMessage.builder()
                                                 .id(rtmMessageId.incrementAndGet())
                                                 .build()
                                                 .toJSONString());
            } catch (NullPointerException | IllegalStateException e) {
                // Assuming that the session is instanceof TyrusSession
                // and it is closed when these exceptions occur
                logger.debug("The RTM session is closed, reconnecting to Slack", e);

                reconnect();
            } catch (RuntimeException e) {
                logger.warn("Failed to ping Slack", e);
            }
        }
    }

    class GoodbyeEventHandler extends RTMEventHandler<GoodbyeEvent> {
        @Override
        public void handle(GoodbyeEvent event) {
            logger.debug("Received goodbye");

            try {
                rtmClient.disconnect();
            } catch (IOException | RuntimeException e) {
                logger.warn("Failed to disconnect from Slack", e);
            }
        }
    }

    class MessageEventHandler extends RTMEventHandler<MessageEvent> {
        @Override
        public void handle(MessageEvent event) {
            logger.debug("Received text<{}> from channel<{}>", event.getText(), event.getChannel());

            slackService.processEvent(SlackMessageEvent.of(event))
                        .subscribe(Consumers.noop(),
                                   t -> logger.error("Failed to handle event<{}>", event, t));
        }
    }

    class ReactionAddedEventHandler extends RTMEventHandler<ReactionAddedEvent> {
        @Override
        public void handle(ReactionAddedEvent event) {
            logger.debug("Received reaction<{}> from channel<{}>",
                         event.getReaction(), event.getItem().getChannel());

            slackService.processEvent(SlackReactionEvent.of(event))
                        .subscribe(Consumers.noop(),
                                   t -> logger.error("Failed to handle event<{}>", event, t));
        }
    }
}
