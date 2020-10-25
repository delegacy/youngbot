package com.github.delegacy.youngbot.slack;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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
import com.github.delegacy.youngbot.message.MessageResponse;
import com.github.delegacy.youngbot.message.MessageService;
import com.github.delegacy.youngbot.slack.reaction.SlackReactionRequest;
import com.github.delegacy.youngbot.slack.reaction.SlackReactionResponse;
import com.github.delegacy.youngbot.slack.reaction.SlackReactionService;
import com.slack.api.app_backend.events.payload.EventsApiPayload;
import com.slack.api.bolt.App;
import com.slack.api.bolt.context.builtin.EventContext;
import com.slack.api.bolt.request.RequestHeaders;
import com.slack.api.bolt.request.builtin.EventRequest;
import com.slack.api.bolt.response.Response;
import com.slack.api.bolt.util.EventsApiPayloadParser;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import com.slack.api.model.event.MessageEvent;
import com.slack.api.model.event.ReactionAddedEvent;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
@ExtendWith(TextFileParameterResolver.class)
class SlackAppBlockingServiceTest {
    @Mock
    private App app;

    @Mock
    private MessageService messageService;

    @Mock
    private SlackClient slackClient;

    @Mock
    private SlackReactionService slackReactionService;

    private SlackAppBlockingService slackAppBlockingService;

    @BeforeEach
    void beforeEach() throws Exception {
        slackAppBlockingService =
                new SlackAppBlockingService(app, messageService, slackClient, slackReactionService);
    }

    @Test
    void testMessageEventHandlerConfiguration() throws Exception {
        slackAppBlockingService.initialize();

        verify(app).event(eq(MessageEvent.class), any(SlackAppBlockingService.MessageEventHandler.class));
    }

    @Test
    void testMessageEventHandler(@TextFile("slackEventMessage.json") String json,
                                 @Mock EventContext ctx) throws Exception {
        EventsApiPayloadParser.getEventTypeAndSubtype(MessageEvent.class);
        final EventRequest eventRequest = new EventRequest(json, new RequestHeaders(Collections.emptyMap()));
        final EventsApiPayload<MessageEvent> event = EventsApiPayloadParser.buildEventPayload(eventRequest);

        final ChatPostMessageResponse chatPostMessageResponse = new ChatPostMessageResponse();
        chatPostMessageResponse.setOk(true);

        when(messageService.process(any())).thenReturn(
                Flux.just(MessageResponse.of(SlackMessageRequest.of(event.getEvent()), "PONG")));
        when(ctx.ack()).thenReturn(Response.ok());
        when(slackClient.sendMessage(anyString(), anyString(), anyString())).thenReturn(Mono.empty());

        final SlackAppBlockingService.MessageEventHandler messageHandler =
                slackAppBlockingService.new MessageEventHandler();
        final Response response = messageHandler.apply(event, ctx);

        assertThat(response.getStatusCode()).isEqualTo(200);
        verify(slackClient).sendMessage(eq("channel1"), eq("PONG"), eq("1558965076.000200"));
    }

    @Test
    void testReactionAddedEventHandler(@TextFile("slackEventReactionAdded.json") String json,
                                       @Mock EventContext ctx) throws Exception {
        EventsApiPayloadParser.getEventTypeAndSubtype(ReactionAddedEvent.class);
        final EventRequest eventRequest = new EventRequest(json, new RequestHeaders(Collections.emptyMap()));
        final EventsApiPayload<ReactionAddedEvent> event =
                EventsApiPayloadParser.buildEventPayload(eventRequest);

        final ChatPostMessageResponse chatPostMessageResponse = new ChatPostMessageResponse();
        chatPostMessageResponse.setOk(true);

        when(slackReactionService.process(any())).thenReturn(
                Flux.just(SlackReactionResponse.of(SlackReactionRequest.of(event.getEvent()), "text")));
        when(ctx.ack()).thenReturn(Response.ok());
        when(slackClient.sendMessage(anyString(), anyString(), anyString())).thenReturn(Mono.empty());

        final SlackAppBlockingService.ReactionAddedEventHandler handler =
                slackAppBlockingService.new ReactionAddedEventHandler();
        final Response response = handler.apply(event, ctx);

        assertThat(response.getStatusCode()).isEqualTo(200);
        verify(slackClient).sendMessage(eq("channel1"), eq("text"), eq("1603437624.003700"));
    }
}
