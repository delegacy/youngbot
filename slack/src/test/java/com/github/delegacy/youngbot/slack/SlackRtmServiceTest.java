package com.github.delegacy.youngbot.slack;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.github.delegacy.youngbot.slack.SlackRtmService.GoodbyeEventHandler;
import com.github.delegacy.youngbot.slack.SlackRtmService.HelloEventHandler;
import com.github.delegacy.youngbot.slack.SlackRtmService.MessageEventHandler;
import com.github.delegacy.youngbot.slack.SlackRtmService.ReactionAddedEventHandler;
import com.slack.api.model.event.GoodbyeEvent;
import com.slack.api.model.event.MessageEvent;
import com.slack.api.model.event.ReactionAddedEvent;
import com.slack.api.model.event.ReactionAddedEvent.Item;
import com.slack.api.rtm.RTMClient;
import com.slack.api.rtm.RTMCloseHandler;
import com.slack.api.rtm.RTMErrorHandler;
import com.slack.api.rtm.RTMEventsDispatcher;
import com.slack.api.rtm.RTMMessageHandler;

import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class SlackRtmServiceTest {
    @Mock
    private RTMClient rtmClient;

    @Mock
    private SlackService slackService;

    @Mock
    private ScheduledExecutorService scheduledExecutorService;

    @Mock
    private RTMEventsDispatcher rtmEventDispatcher;

    @Mock
    private RTMMessageHandler rtmMessageHandler;

    @InjectMocks
    private SlackRtmService slackRtmService;

    @Test
    void testInit() throws Exception {
        when(rtmEventDispatcher.toMessageHandler()).thenReturn(rtmMessageHandler);

        slackRtmService.init();

        verify(rtmEventDispatcher).register(any(HelloEventHandler.class));
        verify(rtmEventDispatcher).register(any(GoodbyeEventHandler.class));
        verify(rtmEventDispatcher).register(any(MessageEventHandler.class));
        verify(rtmEventDispatcher).register(any(ReactionAddedEventHandler.class));

        verify(rtmClient).addMessageHandler(eq(rtmMessageHandler));
        verify(rtmClient).addErrorHandler(any(RTMErrorHandler.class));
        verify(rtmClient).addCloseHandler(any(RTMCloseHandler.class));
        verify(rtmClient).reconnect();
        verify(scheduledExecutorService).scheduleWithFixedDelay(
                any(SlackRtmService.PingTask.class), anyLong(), anyLong(), any(TimeUnit.class));
    }

    @Test
    void testPingTask_whenSessionIsOpen() throws Exception {
        final var pingTask = slackRtmService.new PingTask();
        pingTask.run();

        verify(rtmClient).sendMessage(anyString());
    }

    @Test
    void testPingTask_whenSessionIsClosed() throws Exception {
        doThrow(IllegalStateException.class).when(rtmClient).sendMessage(anyString());

        final var pingTask = slackRtmService.new PingTask();
        pingTask.run();

        // 1 from configureRtmClient, 1 from pingTask
        verify(rtmClient, atLeastOnce()).reconnect();
    }

    @Test
    void testGoodbyeEventHandler() throws Exception {
        final var goodbyeEventHandler = slackRtmService.new GoodbyeEventHandler();
        goodbyeEventHandler.handle(new GoodbyeEvent());

        verify(rtmClient).disconnect();
    }

    @Test
    void testMessageEventHandler() throws Exception {
        final var event = new MessageEvent();
        event.setChannel("channel");
        event.setText("ping");
        event.setUser("user");
        event.setThreadTs("threadTs");

        when(slackService.processEvent(any())).thenReturn(Mono.empty());

        final var messageHandler = slackRtmService.new MessageEventHandler();
        messageHandler.handle(event);

        verify(slackService).processEvent(any(SlackMessageEvent.class));
    }

    @Test
    void testReactionAddedEventHandler() throws Exception {
        final var event = new ReactionAddedEvent();
        event.setReaction("reaction");
        event.setUser("user");
        final Item item = new Item();
        item.setChannel("channel");
        item.setTs("ts");
        event.setItem(item);

        when(slackService.processEvent(any())).thenReturn(Mono.empty());

        final var handler = slackRtmService.new ReactionAddedEventHandler();
        handler.handle(event);

        verify(slackService).processEvent(any(SlackReactionEvent.class));
    }
}
