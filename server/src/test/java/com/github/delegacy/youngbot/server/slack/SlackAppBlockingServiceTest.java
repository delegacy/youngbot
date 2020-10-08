package com.github.delegacy.youngbot.server.slack;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.github.delegacy.youngbot.server.message.handler.MessageHandlerManager;
import com.github.delegacy.youngbot.server.message.handler.PingMessageHandler;
import com.github.delegacy.youngbot.server.util.junit.TextFile;
import com.github.delegacy.youngbot.server.util.junit.TextFileParameterResolver;
import com.slack.api.app_backend.events.payload.EventsApiPayload;
import com.slack.api.bolt.App;
import com.slack.api.bolt.context.builtin.EventContext;
import com.slack.api.bolt.request.RequestHeaders;
import com.slack.api.bolt.request.builtin.EventRequest;
import com.slack.api.bolt.response.Response;
import com.slack.api.bolt.util.BuilderConfigurator;
import com.slack.api.bolt.util.EventsApiPayloadParser;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import com.slack.api.model.event.MessageEvent;

@ExtendWith(MockitoExtension.class)
@ExtendWith(TextFileParameterResolver.class)
class SlackAppBlockingServiceTest {
    @Mock
    private App app;

    @Mock
    private MessageHandlerManager messageHandlerManager;

    private SlackAppBlockingService slackAppBlockingService;

    @BeforeEach
    void beforeEach() throws Exception {
        slackAppBlockingService = new SlackAppBlockingService(app, messageHandlerManager);
    }

    @Test
    void testMessageEventHandlerConfiguration() throws Exception {
        verify(app).event(eq(MessageEvent.class), any(SlackAppBlockingService.MessageEventHandler.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    void testMessageEventHandler(@TextFile("slackEventMessage.json") String json,
                                 @Mock EventContext ctx) throws Exception {
        EventsApiPayloadParser.getEventTypeAndSubtype(MessageEvent.class);
        final EventRequest eventRequest = new EventRequest(json, new RequestHeaders(Collections.emptyMap()));
        final EventsApiPayload<MessageEvent> event = EventsApiPayloadParser.buildEventPayload(eventRequest);

        final ChatPostMessageResponse chatPostMessageResponse = new ChatPostMessageResponse();
        chatPostMessageResponse.setOk(true);

        when(messageHandlerManager.handlers()).thenReturn(Collections.singletonList(new PingMessageHandler()));
        when(ctx.say(any(BuilderConfigurator.class))).thenReturn(chatPostMessageResponse);
        when(ctx.ack()).thenReturn(Response.ok());

        final SlackAppBlockingService.MessageEventHandler messageHandler =
                slackAppBlockingService.new MessageEventHandler();

        final Response response = messageHandler.apply(event, ctx);
        assertThat(response.getStatusCode()).isEqualTo(200);

        await().atMost(Duration.ofSeconds(1)).untilAsserted(
                () -> verify(ctx).say(any(BuilderConfigurator.class)));
    }
}
