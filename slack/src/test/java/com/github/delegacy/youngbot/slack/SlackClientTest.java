package com.github.delegacy.youngbot.slack;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.slack.api.methods.AsyncMethodsClient;
import com.slack.api.methods.request.chat.ChatDeleteScheduledMessageRequest;
import com.slack.api.methods.request.chat.ChatGetPermalinkRequest;
import com.slack.api.methods.request.chat.ChatPostEphemeralRequest;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import com.slack.api.methods.request.chat.ChatScheduleMessageRequest;
import com.slack.api.methods.request.conversations.ConversationsRepliesRequest;
import com.slack.api.methods.response.chat.ChatDeleteScheduledMessageResponse;
import com.slack.api.methods.response.chat.ChatGetPermalinkResponse;
import com.slack.api.methods.response.chat.ChatPostEphemeralResponse;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import com.slack.api.methods.response.chat.ChatScheduleMessageResponse;
import com.slack.api.methods.response.conversations.ConversationsRepliesResponse;
import com.slack.api.model.Message;

import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class SlackClientTest {
    @Mock
    private AsyncMethodsClient rawClient;

    private SlackClient client;

    @BeforeEach
    void beforeEach() throws Exception {
        client = new SlackClient(rawClient);
    }

    @Test
    void testPostMessage(@Mock ChatPostMessageResponse res, @Mock Message message)
            throws Exception {
        when(res.isOk()).thenReturn(true);
        when(res.getWarning()).thenReturn(null);
        when(res.getMessage()).thenReturn(message);
        when(rawClient.chatPostMessage(any(ChatPostMessageRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(res));

        StepVerifier.create(client.postMessage("channel", "message", "threadTs"))
                    .expectNext(message)
                    .expectComplete()
                    .verify();

        final var captor = ArgumentCaptor.forClass(ChatPostMessageRequest.class);
        verify(rawClient).chatPostMessage(captor.capture());
        final var req = captor.getValue();
        assertThat(req.getChannel()).isEqualTo("channel");
        assertThat(req.getText()).isEqualTo("message");
        assertThat(req.getThreadTs()).isEqualTo("threadTs");
    }

    @Test
    void testPostMessage_notOk(@Mock ChatPostMessageResponse res) throws Exception {
        when(res.isOk()).thenReturn(false);
        when(res.getError()).thenReturn("oops");
        when(rawClient.chatPostMessage(any(ChatPostMessageRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(res));

        StepVerifier.create(client.postMessage("channel", "message"))
                    .expectError(SlackException.class)
                    .verify();
    }

    @Test
    void testPostEphemeral(@Mock ChatPostEphemeralResponse res) throws Exception {
        when(res.isOk()).thenReturn(true);
        when(res.getWarning()).thenReturn(null);
        when(res.getMessageTs()).thenReturn("messageTs");
        when(rawClient.chatPostEphemeral(any(ChatPostEphemeralRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(res));

        StepVerifier.create(client.postEphemeral("channel", "message", "user", "threadTs"))
                    .expectNext("messageTs")
                    .expectComplete()
                    .verify();

        final var captor = ArgumentCaptor.forClass(ChatPostEphemeralRequest.class);
        verify(rawClient).chatPostEphemeral(captor.capture());
        final var req = captor.getValue();
        assertThat(req.getChannel()).isEqualTo("channel");
        assertThat(req.getText()).isEqualTo("message");
        assertThat(req.getUser()).isEqualTo("user");
        assertThat(req.getThreadTs()).isEqualTo("threadTs");
        assertThat(req.getAttachments()).isEmpty();
    }

    @Test
    void testPostEphemeral_notOk(@Mock ChatPostEphemeralResponse res) throws Exception {
        when(res.isOk()).thenReturn(false);
        when(res.getError()).thenReturn("oops");
        when(rawClient.chatPostEphemeral(any(ChatPostEphemeralRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(res));

        StepVerifier.create(client.postEphemeral("channel", "message", "user"))
                    .expectError(SlackException.class)
                    .verify();
    }

    @Test
    void testScheduleMessage(@Mock ChatScheduleMessageResponse res) throws Exception {
        when(res.isOk()).thenReturn(true);
        when(res.getWarning()).thenReturn(null);
        when(res.getScheduledMessageId()).thenReturn("scheduledMessageId");
        when(rawClient.chatScheduleMessage(any(ChatScheduleMessageRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(res));

        StepVerifier.create(client.scheduleMessage("channel", "message",
                                                   LocalDateTime.ofInstant(Instant.ofEpochSecond(1609426800L),
                                                                           ZoneId.systemDefault()),
                                                   "threadTs"))
                    .expectNext("scheduledMessageId")
                    .expectComplete()
                    .verify();

        final var captor = ArgumentCaptor.forClass(ChatScheduleMessageRequest.class);
        verify(rawClient).chatScheduleMessage(captor.capture());
        final var req = captor.getValue();
        assertThat(req.getChannel()).isEqualTo("channel");
        assertThat(req.getText()).isEqualTo("message");
        assertThat(req.getPostAt()).isEqualTo(1609426800);
        assertThat(req.getThreadTs()).isEqualTo("threadTs");
    }

    @Test
    void testScheduleMessage_notOk(@Mock ChatScheduleMessageResponse res) throws Exception {
        when(res.isOk()).thenReturn(false);
        when(res.getError()).thenReturn("oops");
        when(rawClient.chatScheduleMessage(any(ChatScheduleMessageRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(res));

        StepVerifier.create(client.scheduleMessage("channel", "message", LocalDateTime.now()))
                    .expectError(SlackException.class)
                    .verify();
    }

    @Test
    void testDeleteScheduledMessage(@Mock ChatDeleteScheduledMessageResponse res) throws Exception {
        when(res.isOk()).thenReturn(true);
        when(rawClient.chatDeleteScheduledMessage(any(ChatDeleteScheduledMessageRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(res));

        StepVerifier.create(client.deleteScheduledMessage("channel", "scheduledMessageId"))
                    .expectComplete()
                    .verify();

        final var captor = ArgumentCaptor.forClass(ChatDeleteScheduledMessageRequest.class);
        verify(rawClient).chatDeleteScheduledMessage(captor.capture());
        final var req = captor.getValue();
        assertThat(req.getChannel()).isEqualTo("channel");
        assertThat(req.getScheduledMessageId()).isEqualTo("scheduledMessageId");
    }

    @Test
    void testDeleteScheduledMessage_notOk(@Mock ChatDeleteScheduledMessageResponse res) throws Exception {
        when(res.isOk()).thenReturn(false);
        when(res.getError()).thenReturn("oops");
        when(rawClient.chatDeleteScheduledMessage(any(ChatDeleteScheduledMessageRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(res));

        StepVerifier.create(client.deleteScheduledMessage("channel", "scheduledMessageId"))
                    .expectError(SlackException.class)
                    .verify();
    }

    @Test
    void testGetPermalink(@Mock ChatGetPermalinkResponse res) throws Exception {
        when(res.isOk()).thenReturn(true);
        when(res.getPermalink()).thenReturn("permalink");
        when(rawClient.chatGetPermalink(any(ChatGetPermalinkRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(res));

        StepVerifier.create(client.getPermalink("channel", "messageTs"))
                    .expectNext("permalink")
                    .expectComplete()
                    .verify();

        final var captor = ArgumentCaptor.forClass(ChatGetPermalinkRequest.class);
        verify(rawClient).chatGetPermalink(captor.capture());
        final var req = captor.getValue();
        assertThat(req.getChannel()).isEqualTo("channel");
        assertThat(req.getMessageTs()).isEqualTo("messageTs");
    }

    @Test
    void testGetPermalink_notOk(@Mock ChatGetPermalinkResponse res) throws Exception {
        when(res.isOk()).thenReturn(false);
        when(rawClient.chatGetPermalink(any(ChatGetPermalinkRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(res));

        StepVerifier.create(client.getPermalink("channel", "messageTs"))
                    .expectError(SlackException.class)
                    .verify();
    }

    @Test
    void testGetThreadOfMessages(@Mock ConversationsRepliesResponse res, @Mock Message message)
            throws Exception {
        when(res.isOk()).thenReturn(true);
        when(res.getMessages()).thenReturn(List.of(message));
        when(rawClient.conversationsReplies(any(ConversationsRepliesRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(res));

        StepVerifier.create(client.getThreadOfMessages("channel", "ts"))
                    .expectNext(List.of(message))
                    .expectComplete()
                    .verify();

        final var captor = ArgumentCaptor.forClass(ConversationsRepliesRequest.class);
        verify(rawClient).conversationsReplies(captor.capture());
        final var req = captor.getValue();
        assertThat(req.getChannel()).isEqualTo("channel");
        assertThat(req.getTs()).isEqualTo("ts");
    }

    @Test
    void testGetThreadOfMessages_notOk(@Mock ConversationsRepliesResponse res) throws Exception {
        when(res.isOk()).thenReturn(false);
        when(rawClient.conversationsReplies(any(ConversationsRepliesRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(res));

        StepVerifier.create(client.getThreadOfMessages("channel", "ts"))
                    .expectError(SlackException.class)
                    .verify();
    }
}
