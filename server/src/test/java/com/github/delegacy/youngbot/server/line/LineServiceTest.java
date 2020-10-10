package com.github.delegacy.youngbot.server.line;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.github.delegacy.youngbot.server.message.MessageContext;
import com.github.delegacy.youngbot.server.message.handler.EchoMessageHandler;
import com.github.delegacy.youngbot.server.message.handler.MessageHandler;
import com.github.delegacy.youngbot.server.message.handler.MessageHandlerManager;
import com.github.delegacy.youngbot.server.message.handler.PingMessageHandler;

import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.event.CallbackRequest;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.event.source.UserSource;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.response.BotApiResponse;

import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class LineServiceTest {
    private static class Echo5MessageHandler implements MessageHandler {
        @Override
        public Pattern pattern() {
            return Pattern.compile("^/?echo5\\s+(.+)$");
        }

        @Override
        public Flux<String> handle(MessageContext msgCtx, Matcher matcher) {
            final String arg = matcher.group(1);
            return Flux.just(arg).repeat(5);
        }
    }

    private static class Echo6MessageHandler implements MessageHandler {
        @Override
        public Pattern pattern() {
            return Pattern.compile("^/?echo6\\s+(.+)$");
        }

        @Override
        public Flux<String> handle(MessageContext msgCtx, Matcher matcher) {
            final String arg = matcher.group(1);
            return Flux.just(arg).repeat(6);
        }
    }

    @Mock
    private LineMessagingClient lineMessagingClient;

    @Mock
    private MessageHandlerManager messageHandlerManager;

    private LineService lineService;

    @BeforeEach
    void beforeEach() throws Exception {
        when(lineMessagingClient.replyMessage(any()))
                .thenReturn(CompletableFuture.completedFuture(
                        new BotApiResponse("requestId", "message", Collections.emptyList())));

        lineService = new LineService(lineMessagingClient, messageHandlerManager);
    }

    @Test
    void testHandleCallback() throws Exception {
        when(messageHandlerManager.handlers()).thenReturn(Collections.singletonList(new PingMessageHandler()));

        final CallbackRequest callback =
                CallbackRequest.builder()
                               .events(Collections.singletonList(
                                       MessageEvent.builder()
                                                   .replyToken("replyToken")
                                                   .source(UserSource.builder().userId("userId").build())
                                                   .message(TextMessageContent.builder()
                                                                              .text("ping")
                                                                              .build())
                                                   .build()))
                               .build();

        StepVerifier.create(lineService.handleCallback(callback))
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
    void testHandleCallback_shouldLimitMessagesInReplyMessage() throws Exception {
        when(messageHandlerManager.handlers()).thenReturn(Collections.singletonList(new Echo6MessageHandler()));

        final CallbackRequest callback =
                CallbackRequest.builder()
                               .events(Collections.singletonList(
                                       MessageEvent.builder()
                                                   .replyToken("replyToken")
                                                   .source(UserSource.builder().userId("userId").build())
                                                   .message(TextMessageContent.builder()
                                                                              .text("echo6 hi")
                                                                              .build())
                                                   .build()))
                               .build();

        StepVerifier.create(lineService.handleCallback(callback))
                    .expectComplete()
                    .verify();

        final ArgumentCaptor<ReplyMessage> replyMessage = ArgumentCaptor.forClass(ReplyMessage.class);
        verify(lineMessagingClient).replyMessage(replyMessage.capture());

        assertThat(replyMessage.getValue().getReplyToken()).isEqualTo("replyToken");
        assertThat(replyMessage.getValue().getMessages().size()).isEqualTo(5);
        final TextMessage textMessage = (TextMessage) replyMessage.getValue().getMessages().get(0);
        assertThat(textMessage.getText()).isEqualTo("hi");
    }

    @Test
    void testHandleCallback_multipleMessageContexts() throws Exception {
        when(messageHandlerManager.handlers()).thenReturn(
                Arrays.asList(new PingMessageHandler(), new EchoMessageHandler(), new Echo5MessageHandler()));

        final CallbackRequest callback =
                CallbackRequest.builder()
                               .events(Arrays.asList(
                                       MessageEvent.builder()
                                                   .replyToken("replyToken1")
                                                   .source(UserSource.builder().userId("userId1").build())
                                                   .message(TextMessageContent.builder().text("ping").build())
                                                   .build(),
                                       MessageEvent.builder()
                                                   .replyToken("replyToken2")
                                                   .source(UserSource.builder().userId("userId2").build())
                                                   .message(TextMessageContent.builder()
                                                                              .text("echo hi")
                                                                              .build())
                                                   .build(),
                                       MessageEvent.builder()
                                                   .replyToken("replyToken3")
                                                   .source(UserSource.builder().userId("userId1").build())
                                                   .message(TextMessageContent.builder()
                                                                              .text("echo5 hello")
                                                                              .build())
                                                   .build()))
                               .build();

        StepVerifier.create(lineService.handleCallback(callback))
                    .expectComplete()
                    .verify();

        final ArgumentCaptor<ReplyMessage> replyMessage = ArgumentCaptor.forClass(ReplyMessage.class);
        verify(lineMessagingClient, times(3)).replyMessage(replyMessage.capture());

        final ReplyMessage replyMessage1 = replyMessage.getAllValues().get(0);
        assertThat(replyMessage1.getReplyToken()).isEqualTo("replyToken1");
        final TextMessage textMessage1 = (TextMessage) replyMessage1.getMessages().get(0);
        assertThat(textMessage1.getText()).isEqualTo("PONG");

        final ReplyMessage replyMessage2 = replyMessage.getAllValues().get(1);
        assertThat(replyMessage2.getReplyToken()).isEqualTo("replyToken2");
        final TextMessage textMessage2 = (TextMessage) replyMessage2.getMessages().get(0);
        assertThat(textMessage2.getText()).isEqualTo("hi");

        final ReplyMessage replyMessage3 = replyMessage.getAllValues().get(2);
        assertThat(replyMessage3.getReplyToken()).isEqualTo("replyToken3");
        assertThat(replyMessage3.getMessages().size()).isEqualTo(5);
        final TextMessage textMessage3 = (TextMessage) replyMessage3.getMessages().get(0);
        assertThat(textMessage3.getText()).isEqualTo("hello");
    }
}
