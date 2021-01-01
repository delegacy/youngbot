package com.github.delegacy.youngbot.line;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.response.BotApiResponse;

import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class LineClientTest {
    @Mock
    private LineMessagingClient rawClient;

    @InjectMocks
    private LineClient client;

    @Test
    void testReplyMessage() throws Exception {
        when(rawClient.replyMessage(any(ReplyMessage.class))).thenReturn(
                CompletableFuture.completedFuture(new BotApiResponse("requestId", "message",
                                                                     Collections.emptyList())));

        StepVerifier.create(client.replyMessage("replyToken", List.of("PONG")))
                    .expectComplete()
                    .verify();

        final var captor = ArgumentCaptor.forClass(ReplyMessage.class);
        verify(rawClient).replyMessage(captor.capture());
        final var textMessage = (TextMessage) captor.getValue().getMessages().get(0);
        assertThat(textMessage.getText()).isEqualTo("PONG");
    }
}
