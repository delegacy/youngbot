package com.github.delegacy.youngbot.slack;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.github.delegacy.youngbot.event.Event;
import com.github.delegacy.youngbot.event.EventResponse;
import com.github.delegacy.youngbot.event.EventService;
import com.slack.api.model.Message;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class SlackServiceTest {
    @Mock
    private EventService eventService;

    @Mock
    private SlackClient slackClient;

    @InjectMocks
    private SlackService slackService;

    @Test
    void testProcessEvent(@Mock Message message) throws Exception {
        final var event = SlackMessageEvent.of("channel", "ping", "user", "threadTs");
        when(eventService.process(any(Event.class))).thenReturn(Flux.just(EventResponse.of("PONG")));
        when(slackClient.postMessage(anyString(), anyString(), anyString())).thenReturn(Mono.just(message));
        when(message.getTs()).thenReturn("messageTs");

        StepVerifier.create(slackService.processEvent(event))
                    .expectComplete()
                    .verify();

        verify(slackClient).postMessage(eq("channel"), eq("PONG"), eq("threadTs"));
    }

    @Test
    void testProcessEvent_ephemeral() throws Exception {
        final var event = SlackReactionEvent.of("channel", ":+1:", "user", "threadTs");
        when(eventService.process(any(Event.class)))
                .thenReturn(Flux.just(SlackEventResponse.builder("PONG")
                                                        .ephemeral(true)
                                                        .build()));
        when(slackClient.postEphemeral(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(Mono.just("messageTs"));

        StepVerifier.create(slackService.processEvent(event))
                    .expectComplete()
                    .verify();

        verify(slackClient).postEphemeral(eq("channel"), eq("PONG"), eq("user"), eq("threadTs"));
    }

    @Test
    void testProcessEvent_notReplyable(@Mock SlackEvent event) throws Exception {
        when(eventService.process(any(Event.class))).thenReturn(Flux.just(EventResponse.of("PONG")));

        StepVerifier.create(slackService.processEvent(event))
                    .expectComplete()
                    .verify();

        verify(slackClient, never()).postMessage(anyString(), anyString(), anyString());
        verify(slackClient, never()).postEphemeral(anyString(), anyString(), anyString(), anyString());
    }
}
