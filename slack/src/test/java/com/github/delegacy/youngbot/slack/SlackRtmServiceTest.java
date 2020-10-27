package com.github.delegacy.youngbot.slack;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.github.delegacy.youngbot.message.MessageResponse;
import com.github.delegacy.youngbot.message.MessageService;
import com.github.delegacy.youngbot.slack.SlackRtmService.GoodbyeEventHandler;
import com.github.delegacy.youngbot.slack.SlackRtmService.HelloEventHandler;
import com.github.delegacy.youngbot.slack.SlackRtmService.MessageEventHandler;
import com.github.delegacy.youngbot.slack.SlackRtmService.ReactionAddedEventHandler;
import com.github.delegacy.youngbot.slack.reaction.SlackReactionRequest;
import com.github.delegacy.youngbot.slack.reaction.SlackReactionResponse;
import com.github.delegacy.youngbot.slack.reaction.SlackReactionService;
import com.slack.api.model.event.GoodbyeEvent;
import com.slack.api.model.event.HelloEvent;
import com.slack.api.model.event.MessageEvent;
import com.slack.api.model.event.ReactionAddedEvent;
import com.slack.api.model.event.ReactionAddedEvent.Item;
import com.slack.api.rtm.RTMClient;
import com.slack.api.rtm.RTMCloseHandler;
import com.slack.api.rtm.RTMErrorHandler;
import com.slack.api.rtm.RTMEventsDispatcher;
import com.slack.api.rtm.RTMMessageHandler;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class SlackRtmServiceTest {
    @Mock
    private RTMClient rtmClient;

    @Mock
    private MessageService messageService;

    @Mock
    private SlackClient slackClient;

    @Mock
    private SlackReactionService slackReactionService;

    @Mock
    private ScheduledExecutorService scheduledExecutorService;

    @Mock
    private RTMEventsDispatcher rtmEventDispatcher;

    @Mock
    private RTMMessageHandler rtmMessageHandler;

    private SlackRtmService slackRtmService;

    @BeforeEach
    void beforeEach() throws Exception {
        slackRtmService = new SlackRtmService(rtmClient, messageService, slackClient, slackReactionService,
                                              scheduledExecutorService, rtmEventDispatcher);
    }

    @Test
    void testInit() throws Exception {
        when(rtmEventDispatcher.toMessageHandler()).thenReturn(rtmMessageHandler);

        slackRtmService.initialize();

        verify(rtmEventDispatcher).register(any(HelloEventHandler.class));
        verify(rtmEventDispatcher).register(any(GoodbyeEventHandler.class));
        verify(rtmEventDispatcher).register(any(MessageEventHandler.class));
        verify(rtmEventDispatcher).register(any(ReactionAddedEventHandler.class));

        verify(rtmClient).addMessageHandler(eq(rtmMessageHandler));
        verify(rtmClient).addErrorHandler(any(RTMErrorHandler.class));
        verify(rtmClient).addCloseHandler(any(RTMCloseHandler.class));
        verify(rtmClient).reconnect();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    void testHelloEventHandler_whenPingTaskIsNull(@Mock ScheduledFuture future) throws Exception {
        when(scheduledExecutorService.scheduleWithFixedDelay(
                any(SlackRtmService.PingTask.class), anyLong(), anyLong(), any(TimeUnit.class)))
                .thenReturn(future);

        final SlackRtmService.HelloEventHandler helloEventHandler = slackRtmService.new HelloEventHandler();
        helloEventHandler.handle(new HelloEvent());

        verify(scheduledExecutorService).scheduleWithFixedDelay(
                any(SlackRtmService.PingTask.class), anyLong(), anyLong(), any(TimeUnit.class));
        assertSame(future, slackRtmService.getPingTaskFuture());
    }

    @SuppressWarnings("rawtypes")
    @Test
    void testHelloEventHandler_whenPingTaskIsNotNull(@Mock ScheduledFuture future) throws Exception {
        final SlackRtmService.HelloEventHandler helloEventHandler = slackRtmService.new HelloEventHandler();
        slackRtmService.setPingTaskFuture(future);

        helloEventHandler.handle(new HelloEvent());

        verify(scheduledExecutorService, never()).scheduleWithFixedDelay(
                any(SlackRtmService.PingTask.class), anyLong(), anyLong(), any(TimeUnit.class));
        assertSame(future, slackRtmService.getPingTaskFuture());
    }

    @Test
    void testPingTask_whenSessionIsOpen() throws Exception {
        final SlackRtmService.PingTask pingTask = slackRtmService.new PingTask();

        pingTask.run();

        verify(rtmClient).sendMessage(anyString());
    }

    @Test
    void testPingTask_whenSessionIsClosed() throws Exception {
        doThrow(IllegalStateException.class).when(rtmClient).sendMessage(anyString());

        final SlackRtmService.PingTask pingTask = slackRtmService.new PingTask();

        pingTask.run();

        // 1 from configureRtmClient, 1 from pingTask
        verify(rtmClient, atLeastOnce()).reconnect();
    }

    @Test
    void testGoodbyeEventHandler() throws Exception {
        final GoodbyeEventHandler goodbyeEventHandler = slackRtmService.new GoodbyeEventHandler();

        goodbyeEventHandler.handle(new GoodbyeEvent());

        verify(rtmClient).disconnect();
    }

    @Test
    void testMessageEventHandler() throws Exception {
        final MessageEvent event = new MessageEvent();
        event.setChannel("channel");
        event.setText("ping");
        event.setThreadTs("threadTs");

        when(messageService.process(any())).thenReturn(
                Flux.just(MessageResponse.of(SlackMessageRequest.of(event), "PONG")));
        when(slackClient.sendMessage(anyString(), anyString(), anyString())).thenReturn(Mono.empty());

        final SlackRtmService.MessageEventHandler messageHandler =
                slackRtmService.new MessageEventHandler();
        messageHandler.handle(event);

        verify(slackClient).sendMessage(eq("channel"), eq("PONG"), eq("threadTs"));
    }

    @Test
    void testReactionAddedEventHandler() throws Exception {
        final ReactionAddedEvent event = new ReactionAddedEvent();
        event.setReaction("reaction");
        event.setUser("user");
        final Item item = new Item();
        item.setChannel("channel");
        item.setTs("messageTs");
        event.setItem(item);

        when(slackReactionService.process(any())).thenReturn(
                Flux.just(SlackReactionResponse.of(SlackReactionRequest.of(event), "text")));
        when(slackClient.sendMessage(anyString(), anyString(), anyString())).thenReturn(Mono.empty());

        final SlackRtmService.ReactionAddedEventHandler handler =
                slackRtmService.new ReactionAddedEventHandler();
        handler.handle(event);

        verify(slackClient).sendMessage(eq("channel"), eq("text"), eq("messageTs"));
    }
}
