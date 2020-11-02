package com.github.delegacy.youngbot.slack;

import static java.util.Objects.requireNonNull;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.slack.api.methods.AsyncMethodsClient;
import com.slack.api.methods.request.chat.ChatDeleteScheduledMessageRequest;
import com.slack.api.methods.request.chat.ChatGetPermalinkRequest;
import com.slack.api.methods.request.chat.ChatPostEphemeralRequest;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import com.slack.api.methods.request.chat.ChatScheduleMessageRequest;
import com.slack.api.methods.request.conversations.ConversationsRepliesRequest;
import com.slack.api.model.Message;

import reactor.core.publisher.Mono;

/**
 * TBW.
 */
public class SlackClient {
    private static final Logger logger = LoggerFactory.getLogger(SlackClient.class);

    private final AsyncMethodsClient client;

    /**
     * TBW.
     */
    public SlackClient(AsyncMethodsClient client) {
        this.client = requireNonNull(client, "client");
    }

    /**
     * TBW.
     */
    public AsyncMethodsClient client() {
        return client;
    }

    /**
     * TBW.
     */
    public Mono<Message> postMessage(String channel, String message) {
        return postMessage(channel, message, null);
    }

    /**
     * TBW.
     */
    public Mono<Message> postMessage(String channel, String message, @Nullable String threadTs) {
        return Mono.just(ChatPostMessageRequest.builder()
                                               .channel(requireNonNull(channel, "channel"))
                                               .text(requireNonNull(message, "message"))
                                               .threadTs(threadTs)
                                               .build())
                   .flatMap(req -> Mono.fromFuture(client.chatPostMessage(req)))
                   .flatMap(res -> {
                       if (!res.isOk()) {
                           logger.error("Failed to post message<{}> in channel<{}>;error<{}>",
                                        message, channel, res.getError());
                           return Mono.empty();
                       }

                       if (res.getWarning() != null) {
                           logger.warn("Posted message<{}> in channel<{}>;warn<{}>",
                                       message, channel, res.getWarning());
                       } else {
                           logger.debug("Posted message<{}> in channel<{}>", message, channel);
                       }
                       return Mono.just(res.getMessage());
                   });
    }

    /**
     * TBW.
     */
    public Mono<String> postEphemeral(String channel, String message, String user) {
        return postEphemeral(channel, message, user, null);
    }

    /**
     * TBW.
     */
    public Mono<String> postEphemeral(String channel, String message, String user, @Nullable String threadTs) {
        return Mono.just(ChatPostEphemeralRequest.builder()
                                                 .channel(requireNonNull(channel, "channel"))
                                                 .text(requireNonNull(message, "message"))
                                                 .user(requireNonNull(user, "user"))
                                                 .threadTs(threadTs)
                                                 .attachments(List.of())
                                                 .build())
                   .flatMap(req -> Mono.fromFuture(client.chatPostEphemeral(req)))
                   .flatMap(res -> {
                       if (!res.isOk()) {
                           logger.error(
                                   "Failed to post ephemeral message<{}> to user<{}> in channel<{}>;error<{}>",
                                   message, user, channel, res.getError());
                           return Mono.empty();
                       }

                       if (res.getWarning() != null) {
                           logger.warn("Posted ephemeral message<{}> to user<{}> in channel<{}>;warn<{}>",
                                       message, user, channel, res.getWarning());
                       } else {
                           logger.debug("Posted ephemeral message<{}> to user<{}> in channel<{}>",
                                        message, user, channel);
                       }
                       return Mono.just(res.getMessageTs());
                   });
    }

    /**
     * TBW.
     */
    public Mono<String> scheduleMessage(String channel, String message, LocalDateTime postAt) {
        return scheduleMessage(channel, message, postAt, null);
    }

    /**
     * TBW.
     */
    public Mono<String> scheduleMessage(String channel, String message, LocalDateTime postAt,
                                        @Nullable String threadTs) {
        return Mono.just(ChatScheduleMessageRequest.builder()
                                                   .channel(requireNonNull(channel, "channel"))
                                                   .text(requireNonNull(message, "message"))
                                                   .postAt((int) requireNonNull(postAt, "postAt")
                                                           .atZone(ZoneId.systemDefault()).toEpochSecond())
                                                   .threadTs(threadTs)
                                                   .build())
                   .flatMap(req -> Mono.fromFuture(client.chatScheduleMessage(req)))
                   .flatMap(res -> {
                       if (!res.isOk()) {
                           logger.error("Failed to schedule message<{}> at <{}> in channel<{}>;error<{}>",
                                        message, channel, postAt, res.getError());
                           return Mono.empty();
                       }

                       if (res.getWarning() != null) {
                           logger.warn("Posted schedule message<{}> at <{}> in channel<{}>;warn<{}>",
                                       message, channel, postAt, res.getWarning());
                       } else {
                           logger.debug("Posted schedule message<{}> at <{}> in channel<{}>",
                                        message, channel, postAt);
                       }
                       return Mono.just(res.getScheduledMessageId());
                   });
    }

    /**
     * TBW.
     */
    public Mono<Void> deleteScheduledMessage(String channel, String scheduledMessageId) {
        return Mono.just(ChatDeleteScheduledMessageRequest
                                 .builder()
                                 .channel(requireNonNull(channel, "channel"))
                                 .scheduledMessageId(requireNonNull(scheduledMessageId, "scheduledMessageId"))
                                 .build())
                   .flatMap(req -> Mono.fromFuture(client.chatDeleteScheduledMessage(req)))
                   .doOnNext(res -> {
                       if (!res.isOk()) {
                           logger.error("Failed to delete scheduledMessage<{}> in channel<{}>;error<{}>",
                                        scheduledMessageId, channel, res.getError());
                       }

                       logger.debug("Deleted scheduledMessage<{}> in channel<{}>", scheduledMessageId, channel);
                   })
                   .then();
    }

    /**
     * TBW.
     */
    public Mono<String> getPermalink(String channel, String messageTs) {
        return Mono.just(ChatGetPermalinkRequest.builder()
                                                .channel(requireNonNull(channel, "channel"))
                                                .messageTs(requireNonNull(messageTs, "messageTs"))
                                                .build())
                   .flatMap(req -> Mono.fromFuture(client.chatGetPermalink(req)))
                   .flatMap(res -> {
                       if (!res.isOk()) {
                           logger.warn("Failed to getPermalink for messageTs<{}> in channel<{}>;error<{}>",
                                       messageTs, channel, res.getError());
                           return Mono.empty();
                       }

                       return Mono.just(res.getPermalink());
                   });
    }

    /**
     * TBW.
     */
    public Mono<List<Message>> getThreadedMessages(String channel, String messageTs) {
        return Mono.just(ConversationsRepliesRequest.builder()
                                                    .channel(requireNonNull(channel, "channel"))
                                                    .ts(requireNonNull(messageTs, "messageTs"))
                                                    .build())
                   .flatMap(req -> Mono.fromFuture(client.conversationsReplies(req)))
                   .flatMap(res -> {
                       if (!res.isOk()) {
                           logger.warn(
                                   "Failed to getThreadedMessages for messageTs<{}> in channel<{}>;error<{}>",
                                   messageTs, channel, res.getError());
                           return Mono.empty();
                       }

                       return Mono.just(res.getMessages());
                   });
    }
}
