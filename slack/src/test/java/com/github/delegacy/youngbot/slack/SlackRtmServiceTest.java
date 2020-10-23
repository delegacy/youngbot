package com.github.delegacy.youngbot.slack;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.github.delegacy.youngbot.message.MessageResponse;
import com.github.delegacy.youngbot.message.MessageService;
import com.github.delegacy.youngbot.slack.SlackRtmService.MessageWithThreadTs;
import com.slack.api.Slack;
import com.slack.api.bolt.App;
import com.slack.api.bolt.AppConfig;
import com.slack.api.model.event.HelloEvent;
import com.slack.api.model.event.MessageEvent;
import com.slack.api.rtm.RTMClient;
import com.slack.api.rtm.RTMCloseHandler;
import com.slack.api.rtm.RTMErrorHandler;
import com.slack.api.rtm.RTMMessageHandler;
import com.slack.api.util.json.GsonFactory;

import reactor.core.publisher.Flux;

@ExtendWith(MockitoExtension.class)
class SlackRtmServiceTest {
    @Mock
    private RTMClient rtmClient;

    @Mock
    private MessageService messageService;

    @Mock
    private ScheduledExecutorService scheduledExecutorService;

    private SlackRtmService slackRtmService;

    @BeforeEach
    void beforeEach(@Mock App app, @Mock AppConfig appConfig, @Mock Slack slack) throws Exception {
        when(app.slack()).thenReturn(slack);
        when(app.config()).thenReturn(appConfig);
        when(appConfig.getSingleTeamBotToken()).thenReturn("bot-token");
        when(slack.rtmConnect(anyString())).thenReturn(rtmClient);

        slackRtmService = new SlackRtmService(app, messageService, scheduledExecutorService);
    }

    @Test
    void testConfigureRtmClient() throws Exception {
        verify(rtmClient).addMessageHandler(any(RTMMessageHandler.class));
        verify(rtmClient).addErrorHandler(any(RTMErrorHandler.class));
        verify(rtmClient).addCloseHandler(any(RTMCloseHandler.class));
        verify(rtmClient).reconnect();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    void testHelloEventHandler_whenPingTaskIsNull(@Mock ScheduledFuture future) throws Exception {
        final SlackRtmService.HelloEventHandler helloEventHandler = slackRtmService.new HelloEventHandler();
        when(scheduledExecutorService.scheduleWithFixedDelay(
                any(SlackRtmService.PingTask.class), anyLong(), anyLong(), any(TimeUnit.class)))
                .thenReturn(future);

        helloEventHandler.handle(new HelloEvent());

        assertThat(slackRtmService.sessionClosed).isFalse();
        verify(scheduledExecutorService).scheduleWithFixedDelay(
                any(SlackRtmService.PingTask.class), anyLong(), anyLong(), any(TimeUnit.class));
        assertSame(future, slackRtmService.pingTaskFuture);
    }

    @SuppressWarnings("rawtypes")
    @Test
    void testHelloEventHandler_whenPingTaskIsNotNull(@Mock ScheduledFuture future) throws Exception {
        final SlackRtmService.HelloEventHandler helloEventHandler = slackRtmService.new HelloEventHandler();
        slackRtmService.pingTaskFuture = future;

        helloEventHandler.handle(new HelloEvent());

        assertThat(slackRtmService.sessionClosed).isFalse();
        verify(scheduledExecutorService, never()).scheduleWithFixedDelay(
                any(SlackRtmService.PingTask.class), anyLong(), anyLong(), any(TimeUnit.class));
        assertSame(future, slackRtmService.pingTaskFuture);
    }

    @Test
    void testPingTask_whenSessionIsOpen() throws Exception {
        final SlackRtmService.PingTask pingTask = slackRtmService.new PingTask();
        slackRtmService.sessionClosed = false;

        pingTask.run();

        verify(rtmClient).sendMessage(anyString());
    }

    @Test
    void testPingTask_whenSessionIsClosed() throws Exception {
        final SlackRtmService.PingTask pingTask = slackRtmService.new PingTask();
        slackRtmService.sessionClosed = true;

        pingTask.run();

        // 1 from configureRtmClient, 1 from pingTask
        verify(rtmClient, times(2)).reconnect();
    }

    @Test
    void testMessageEventHandler() throws Exception {
        final MessageEvent event = new MessageEvent();
        event.setChannel("channel");
        event.setText("ping");
        event.setThreadTs("thread");

        when(messageService.process(any())).thenReturn(
                Flux.just(MessageResponse.of(SlackMessageRequest.of(event), "PONG")));

        final SlackRtmService.MessageEventHandler messageHandler = slackRtmService.new MessageEventHandler();

        messageHandler.handle(event);

        await().atMost(Duration.ofSeconds(1)).untilAsserted(() -> {
            final ArgumentCaptor<String> textCaptor = ArgumentCaptor.forClass(String.class);
            verify(rtmClient).sendMessage(textCaptor.capture());

            final MessageWithThreadTs msg = GsonFactory.createSnakeCase()
                                                       .fromJson(textCaptor.getValue(),
                                                                 MessageWithThreadTs.class);
            assertThat(msg.getText()).isEqualTo("PONG");
            assertThat(msg.getChannel()).isEqualTo("channel");
            assertThat(msg.getThreadTs()).isEqualTo("thread");
        });
    }
}
