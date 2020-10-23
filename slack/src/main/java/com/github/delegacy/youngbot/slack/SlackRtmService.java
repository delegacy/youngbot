package com.github.delegacy.youngbot.slack;

import static java.util.Objects.requireNonNull;

import java.io.Closeable;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.Nullable;
import javax.websocket.CloseReason.CloseCodes;
import javax.websocket.DeploymentException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.delegacy.youngbot.message.MessageService;
import com.google.common.annotations.VisibleForTesting;
import com.slack.api.bolt.App;
import com.slack.api.methods.SlackApiException;
import com.slack.api.model.event.GoodbyeEvent;
import com.slack.api.model.event.HelloEvent;
import com.slack.api.model.event.MessageEvent;
import com.slack.api.rtm.RTMClient;
import com.slack.api.rtm.RTMEventHandler;
import com.slack.api.rtm.RTMEventsDispatcher;
import com.slack.api.rtm.RTMEventsDispatcherFactory;
import com.slack.api.rtm.message.Message;
import com.slack.api.rtm.message.PingMessage;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.reactor.ratelimiter.operator.RateLimiterOperator;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * TBW.
 */
public class SlackRtmService implements Closeable {
    private static final Logger logger = LoggerFactory.getLogger(SlackRtmService.class);

    private final AtomicLong msgId = new AtomicLong();

    private final ScheduledExecutorService executorService;

    private final RTMClient rtmClient;

    private final MessageService messageService;

    // Respect the rate limit for write, https://api.slack.com/docs/rate-limits#rtm
    private final RateLimiter rateLimiterWrite =
            RateLimiter.of("write",
                           RateLimiterConfig.custom()
                                            .limitForPeriod(1)
                                            .limitRefreshPeriod(Duration.ofSeconds(1))
                                            .build());

    // Respect the rate limit for reconnect, https://api.slack.com/docs/rate-limits#rtm
    private final RateLimiter rateLimiterReconnect =
            RateLimiter.of("reconnect",
                           RateLimiterConfig.custom()
                                            .limitForPeriod(1)
                                            .limitRefreshPeriod(Duration.ofMinutes(1))
                                            .build());

    @VisibleForTesting
    volatile boolean sessionClosed = true;

    @VisibleForTesting
    @Nullable
    ScheduledFuture<?> pingTaskFuture;

    /**
     * TBW.
     *
     * @throws Exception TBW
     */
    public SlackRtmService(App app, MessageService messageService) throws Exception {
        this(app, messageService, Executors.newSingleThreadScheduledExecutor());
    }

    @VisibleForTesting
    SlackRtmService(App app, MessageService messageService, ScheduledExecutorService executorService)
            throws Exception {
        this.messageService = requireNonNull(messageService, "messageService");
        this.executorService = requireNonNull(executorService, "executorService");

        rtmClient = requireNonNull(app, "app").slack().rtmConnect(app.config().getSingleTeamBotToken());
        configureRtmClient();
    }

    private void configureRtmClient() throws Exception {
        final RTMEventsDispatcher dispatcher = RTMEventsDispatcherFactory.getInstance();
        dispatcher.register(new HelloEventHandler());
        dispatcher.register(new MessageEventHandler());
        dispatcher.register(new GoodbyeEventHandler());

        rtmClient.addMessageHandler(dispatcher.toMessageHandler());
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

    @Override
    public void close() throws IOException {
        executorService.shutdown();

        rtmClient.close();
    }

    private void reconnect() {
        if (!rateLimiterReconnect.acquirePermission()) {
            logger.warn("Failed to acquire permission from the rate limiter");
            return;
        }

        try {
            rtmClient.reconnect();
        } catch (IOException | SlackApiException | URISyntaxException | DeploymentException |
                RuntimeException e) {
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

            final SlackMessageRequest msgReq = SlackMessageRequest.of(event);

            messageService.process(msgReq)
                          .flatMap(res -> replyMessage(msgReq, res.text()))
                          .subscribe(null,
                                     t -> logger.error("Failed to handle event<{}>", event, t));
        }

        private Mono<Void> replyMessage(SlackMessageRequest msgReq, String res) {
            return Mono.fromRunnable(
                    () -> {
                        final long id = msgId.incrementAndGet();
                        rtmClient.sendMessage(
                                new MessageWithThreadTs(Message.builder()
                                                               .id(id)
                                                               .channel(msgReq.channel())
                                                               .text(res)
                                                               .build(),
                                                        msgReq.thread()).toJSONString());

                        logger.debug("Replied to text<{}> in channel<{}>;id<{}>",
                                     msgReq.text(), msgReq.channel(), id);
                    })
                       .transformDeferred(RateLimiterOperator.of(rateLimiterWrite))
                       .then()
                       .subscribeOn(Schedulers.boundedElastic());
        }
    }

    /**
     * TBW.
     */
    public static final class MessageWithThreadTs extends Message {
        @Nullable
        private final String threadTs;

        /**
         * TBW.
         */
        public MessageWithThreadTs(Message message, @Nullable String threadTs) {
            super(message.getId(), message.getChannel(), message.getText(),
                  message.getBlocks(), message.getAttachments());

            this.threadTs = threadTs;
        }

        /**
         * TBW.
         */
        @Nullable
        public String getThreadTs() {
            return threadTs;
        }
    }
}
