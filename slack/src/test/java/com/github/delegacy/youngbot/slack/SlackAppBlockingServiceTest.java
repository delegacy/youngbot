package com.github.delegacy.youngbot.slack;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.github.delegacy.youngbot.internal.testing.TextFile;
import com.github.delegacy.youngbot.internal.testing.TextFileParameterResolver;
import com.slack.api.app_backend.events.payload.EventsApiPayload;
import com.slack.api.bolt.App;
import com.slack.api.bolt.context.builtin.EventContext;
import com.slack.api.bolt.request.Request;
import com.slack.api.bolt.request.RequestHeaders;
import com.slack.api.bolt.request.builtin.EventRequest;
import com.slack.api.bolt.response.Response;
import com.slack.api.bolt.util.EventsApiPayloadParser;
import com.slack.api.model.event.MessageEvent;
import com.slack.api.model.event.ReactionAddedEvent;

import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
@ExtendWith(TextFileParameterResolver.class)
class SlackAppBlockingServiceTest {
    @Mock
    private App app;

    @Mock
    private SlackService slackService;

    private SlackAppBlockingService slackAppBlockingService;

    @BeforeEach
    void beforeEach() throws Exception {
        slackAppBlockingService = new SlackAppBlockingService(app, slackService);
    }

    @Test
    void testInit() throws Exception {
        slackAppBlockingService.init();

        verify(app).event(eq(MessageEvent.class), any(SlackAppBlockingService.MessageEventHandler.class));
        verify(app).event(eq(ReactionAddedEvent.class),
                          any(SlackAppBlockingService.ReactionAddedEventHandler.class));
    }

    @Test
    void testRun(@Mock Request<?> request, @Mock Response response) throws Exception {
        when(app.run(eq(request))).thenReturn(response);

        final var actual = slackAppBlockingService.run(request);
        assertThat(actual).isSameAs(response);
    }

    @Test
    void testMessageEventHandler(@TextFile("slackEventMessage.json") String json,
                                 @Mock EventContext ctx) throws Exception {
        EventsApiPayloadParser.getEventTypeAndSubtype(MessageEvent.class);
        final EventRequest eventRequest = new EventRequest(json, new RequestHeaders(Collections.emptyMap()));
        final EventsApiPayload<MessageEvent> event = EventsApiPayloadParser.buildEventPayload(eventRequest);

        when(slackService.processEvent(any())).thenReturn(Mono.empty());
        when(ctx.ack()).thenReturn(Response.ok());

        final SlackAppBlockingService.MessageEventHandler messageHandler =
                slackAppBlockingService.new MessageEventHandler();
        final Response response = messageHandler.apply(event, ctx);

        assertThat(response.getStatusCode()).isEqualTo(200);
        verify(slackService).processEvent(any(SlackMessageEvent.class));
    }

    @Test
    void testReactionAddedEventHandler(@TextFile("slackEventReactionAdded.json") String json,
                                       @Mock EventContext ctx) throws Exception {
        EventsApiPayloadParser.getEventTypeAndSubtype(ReactionAddedEvent.class);
        final EventRequest eventRequest = new EventRequest(json, new RequestHeaders(Collections.emptyMap()));
        final EventsApiPayload<ReactionAddedEvent> event =
                EventsApiPayloadParser.buildEventPayload(eventRequest);

        when(slackService.processEvent(any())).thenReturn(Mono.empty());
        when(ctx.ack()).thenReturn(Response.ok());

        final SlackAppBlockingService.ReactionAddedEventHandler handler =
                slackAppBlockingService.new ReactionAddedEventHandler();
        final Response response = handler.apply(event, ctx);

        assertThat(response.getStatusCode()).isEqualTo(200);
        verify(slackService).processEvent(any(SlackReactionEvent.class));
    }
}
