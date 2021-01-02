package com.github.delegacy.youngbot.line;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.github.delegacy.youngbot.event.EventResponse;
import com.github.delegacy.youngbot.event.EventService;

import com.linecorp.bot.model.event.CallbackRequest;
import com.linecorp.bot.model.event.Event;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.event.source.UserSource;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class LineServiceTest {
    private static CallbackRequest toCallbackRequest(LineMessageEvent event) {
        return toCallbackRequest(List.of(event));
    }

    private static CallbackRequest toCallbackRequest(List<LineMessageEvent> events) {
        return CallbackRequest.builder()
                              .events(toEvents(events))
                              .build();
    }

    private static List<Event> toEvents(List<LineMessageEvent> events) {
        return events.stream()
                     .map(evt -> MessageEvent.builder()
                                             .replyToken(evt.replyToken())
                                             .source(UserSource.builder().userId(evt.channel()).build())
                                             .message(TextMessageContent.builder().text(evt.text()).build())
                                             .build())
                     .collect(Collectors.toUnmodifiableList());
    }

    @Mock
    private EventService eventService;

    @Mock
    private LineClient lineClient;

    @Captor
    private ArgumentCaptor<List<String>> captor;

    @InjectMocks
    private LineService lineService;

    @Test
    void testHandleCallback() throws Exception {
        final LineMessageEvent event = LineMessageEvent.of("userId", "ping", "replyToken");
        when(eventService.process(any())).thenReturn(Flux.just(EventResponse.of("PONG")));
        when(lineClient.replyMessage(anyString(), any())).thenReturn(Mono.empty());

        StepVerifier.create(lineService.handleCallback(toCallbackRequest(event)))
                    .expectComplete()
                    .verify();

        verify(lineClient).replyMessage(eq("replyToken"), captor.capture());

        assertThat(captor.getValue().size()).isEqualTo(1);
        assertThat(captor.getValue().get(0)).isEqualTo("PONG");
    }

    @Test
    void testHandleCallback_shouldFollowLimitOnMessagesPerReply() throws Exception {
        final LineMessageEvent event = LineMessageEvent.of("userId", "ping", "replyToken");
        when(eventService.process(any())).thenReturn(Flux.just(EventResponse.of("PONG")).repeat(5));
        when(lineClient.replyMessage(anyString(), any())).thenReturn(Mono.empty());

        StepVerifier.create(lineService.handleCallback(toCallbackRequest(event)))
                    .expectComplete()
                    .verify();

        verify(lineClient).replyMessage(eq("replyToken"), captor.capture());

        assertThat(captor.getValue().size()).isEqualTo(5);
        assertThat(captor.getValue().get(0)).isEqualTo("PONG");
    }
}
