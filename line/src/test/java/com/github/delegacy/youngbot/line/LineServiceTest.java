package com.github.delegacy.youngbot.line;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.github.delegacy.youngbot.message.MessageResponse;
import com.github.delegacy.youngbot.message.MessageService;

import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.event.CallbackRequest;
import com.linecorp.bot.model.event.Event;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.event.source.UserSource;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.response.BotApiResponse;

import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class LineServiceTest {
    private static CallbackRequest toCallbackRequest(LineMessageRequest req) {
        return toCallbackRequest(List.of(req));
    }

    private static CallbackRequest toCallbackRequest(List<LineMessageRequest> reqs) {
        return CallbackRequest.builder()
                              .events(toEvents(reqs))
                              .build();
    }

    private static List<Event> toEvents(List<LineMessageRequest> reqs) {
        return reqs.stream()
                   .map(req -> MessageEvent.builder()
                                           .replyToken(req.replyToken())
                                           .source(UserSource.builder().userId(req.channel()).build())
                                           .message(TextMessageContent.builder().text(req.text()).build())
                                           .build())
                   .collect(Collectors.toList());
    }

    @Mock
    private LineMessagingClient lineMessagingClient;

    @Mock
    private MessageService messageService;

    private LineService lineService;

    @BeforeEach
    void beforeEach() throws Exception {
        when(lineMessagingClient.replyMessage(any())).thenAnswer(
                m -> CompletableFuture.completedFuture(
                new BotApiResponse(UUID.randomUUID().toString(), "message", Collections.emptyList())));

        lineService = new LineService(lineMessagingClient, messageService);
    }

    @Test
    void testHandleCallback() throws Exception {
        final LineMessageRequest req = new LineMessageRequest("userId", "ping", "replyToken");
        when(messageService.process(any())).thenReturn(Flux.just(MessageResponse.of(req, "PONG")));

        StepVerifier.create(lineService.handleCallback(toCallbackRequest(req)))
                    .expectComplete()
                    .verify();

        final ArgumentCaptor<ReplyMessage> replyMessage = ArgumentCaptor.forClass(ReplyMessage.class);
        verify(lineMessagingClient).replyMessage(replyMessage.capture());

        assertThat(replyMessage.getValue().getReplyToken()).isEqualTo("replyToken");
        assertThat(replyMessage.getValue().getMessages().size()).isEqualTo(1);
        final TextMessage textMessage = (TextMessage) replyMessage.getValue().getMessages().get(0);
        assertThat(textMessage.getText()).isEqualTo("PONG");
    }

    @Test
    void testHandleCallback_shouldFollowLimitOnMessagesPerReply() throws Exception {
        final LineMessageRequest req = new LineMessageRequest("userId", "ping", "replyToken");
        when(messageService.process(any())).thenReturn(Flux.just(MessageResponse.of(req, "PONG")).repeat(6));

        StepVerifier.create(lineService.handleCallback(toCallbackRequest(req)))
                    .expectComplete()
                    .verify();

        final ArgumentCaptor<ReplyMessage> replyMessage = ArgumentCaptor.forClass(ReplyMessage.class);
        verify(lineMessagingClient).replyMessage(replyMessage.capture());

        assertThat(replyMessage.getValue().getReplyToken()).isEqualTo("replyToken");
        assertThat(replyMessage.getValue().getMessages().size()).isEqualTo(5);
        final TextMessage textMessage = (TextMessage) replyMessage.getValue().getMessages().get(0);
        assertThat(textMessage.getText()).isEqualTo("PONG");
    }

    @Test
    void testHandleCallback_multipleLineMessageRequests() throws Exception {
        final LineMessageRequest req1 = new LineMessageRequest("userId1", "ping","replyToken1");
        final LineMessageRequest req2 = new LineMessageRequest("userId2", "ping","replyToken2");
        final LineMessageRequest req3 = new LineMessageRequest("userId1", "ping","replyToken3");
        when(messageService.process(any())).thenReturn(
                Flux.just(MessageResponse.of(req1, "PONG1")),
                Flux.just(MessageResponse.of(req2, "PONG2")),
                Flux.just(MessageResponse.of(req3, "PONG3")).repeat(5));

        StepVerifier.create(lineService.handleCallback(toCallbackRequest(List.of(req1, req2, req3))))
                    .expectComplete()
                    .verify();

        final ArgumentCaptor<ReplyMessage> replyMessage = ArgumentCaptor.forClass(ReplyMessage.class);
        verify(lineMessagingClient, times(3)).replyMessage(replyMessage.capture());

        final ReplyMessage replyMessage1 = replyMessage.getAllValues().get(0);
        assertThat(replyMessage1.getReplyToken()).isEqualTo("replyToken1");
        final TextMessage textMessage1 = (TextMessage) replyMessage1.getMessages().get(0);
        assertThat(textMessage1.getText()).isEqualTo("PONG1");

        final ReplyMessage replyMessage2 = replyMessage.getAllValues().get(1);
        assertThat(replyMessage2.getReplyToken()).isEqualTo("replyToken2");
        final TextMessage textMessage2 = (TextMessage) replyMessage2.getMessages().get(0);
        assertThat(textMessage2.getText()).isEqualTo("PONG2");

        final ReplyMessage replyMessage3 = replyMessage.getAllValues().get(2);
        assertThat(replyMessage3.getReplyToken()).isEqualTo("replyToken3");
        assertThat(replyMessage3.getMessages().size()).isEqualTo(5);
        final TextMessage textMessage3 = (TextMessage) replyMessage3.getMessages().get(0);
        assertThat(textMessage3.getText()).isEqualTo("PONG3");
    }
}
