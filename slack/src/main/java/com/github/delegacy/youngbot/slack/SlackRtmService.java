package com.github.delegacy.youngbot.slack;

import static java.util.Objects.requireNonNull;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import javax.websocket.CloseReason.CloseCodes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.delegacy.youngbot.message.MessageService;
import com.slack.api.bolt.App;
import com.slack.api.model.event.HelloEvent;
import com.slack.api.model.event.MessageEvent;
import com.slack.api.rtm.RTMClient;
import com.slack.api.rtm.RTMEventHandler;
import com.slack.api.rtm.RTMEventsDispatcher;
import com.slack.api.rtm.RTMEventsDispatcherFactory;
import com.slack.api.rtm.message.Message;
import com.slack.api.rtm.message.PingMessage;

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

    /**
     * TBW.
     *
     * @throws Exception TBW
     */
    public SlackRtmService(App app, MessageService messageService) throws Exception {
        this(app, messageService, Executors.newSingleThreadScheduledExecutor());
    }

    /**
     * TBW.
     *
     * @throws Exception TBW
     */
    public SlackRtmService(App app, MessageService messageService, ScheduledExecutorService executorService)
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

        rtmClient.addMessageHandler(dispatcher.toMessageHandler());
        rtmClient.addErrorHandler(t -> logger.warn("A RTM session error occurred.", t));
        rtmClient.addCloseHandler(reason -> {
            if (reason.getCloseCode() == CloseCodes.NORMAL_CLOSURE) {
                logger.info("The RTM session is closed because of {}.", reason);
            } else {
                logger.error("The RTM session is closed because of {}.", reason);
            }
        });

        rtmClient.reconnect();
    }

    @Override
    public void close() throws IOException {
        executorService.shutdown();

        rtmClient.close();
    }

    class HelloEventHandler extends RTMEventHandler<HelloEvent> {
        @Override
        public void handle(HelloEvent event) {
            executorService.scheduleAtFixedRate(new PingTask(), 1L, 30L, TimeUnit.SECONDS);
        }
    }

    class PingTask implements Runnable {
        @Override
        public void run() {
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

    class MessageEventHandler extends RTMEventHandler<MessageEvent> {
        @Override
        public void handle(MessageEvent event) {
            logger.debug("Received text<{}> from channel<{}>", event.getText(), event.getChannel());

            final SlackMessageRequest msgReq =
                    new SlackMessageRequest(event.getText(), event.getChannel());

            messageService.process(msgReq)
                          .flatMap(res -> replyMessage(msgReq, res.text()))
                          .subscribeOn(Schedulers.boundedElastic())
                          .subscribe(null,
                                     t -> logger.error("Failed to handle event<{}>", event, t));
        }

        private Mono<Void> replyMessage(SlackMessageRequest msgReq, String res) {
            return Mono.fromRunnable(() -> {
                final long id = msgId.incrementAndGet();
                rtmClient.sendMessage(Message.builder()
                                             .id(id)
                                             .channel(msgReq.channel())
                                             .text(res)
                                             .build()
                                             .toJSONString());

                logger.debug("Replied to text<{}> in channel<{}>;id<{}>", msgReq.text(), msgReq.channel(), id);
            });
        }
    }
}
