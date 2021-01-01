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
    public AsyncMethodsClient rawClient() {
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
                           logger.error("Failed to send message<{}> to channel<{}>;error<{}>",
                                        message, channel, res.getError());
                           return Mono.error(
                                   new SlackException("Failed to send message;error:" + res.getError()));
                       }

                       if (res.getWarning() == null) {
                           logger.debug("Sent message<{}> to channel<{}>", message, channel);
                       } else {
                           logger.warn("Sent message<{}> to channel<{}>;warn<{}>",
                                       message, channel, res.getWarning());
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
                                   "Failed to send ephemeral message<{}> to user<{}> in channel<{}>;error<{}>",
                                   message, user, channel, res.getError());
                           return Mono.error(
                                   new SlackException("Failed to send ephemeral message;error:" +
                                                      res.getError()));
                       }

                       if (res.getWarning() == null) {
                           logger.debug("Sent ephemeral message<{}> to user<{}> in channel<{}>",
                                        message, user, channel);
                       } else {
                           logger.warn("Sent ephemeral message<{}> to user<{}> in channel<{}>;warn<{}>",
                                       message, user, channel, res.getWarning());
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
                           logger.error("Failed to schedule message<{}> to channel<{}> at <{}>;error<{}>",
                                        message, channel, postAt, res.getError());
                           return Mono.error(
                                   new SlackException("Failed to schedule message;error:" + res.getError()));
                       }

                       if (res.getWarning() == null) {
                           logger.debug("Scheduled message<{}> to channel<{}> at <{}>",
                                        message, channel, postAt);
                       } else {
                           logger.warn("Scheduled message<{}> to channel<{}> at <{}>;warn<{}>",
                                       message, channel, postAt, res.getWarning());
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
                   .map(res -> {
                       if (!res.isOk()) {
                           logger.error(
                                   "Failed to delete pending scheduledMessage<{}> in channel<{}>;error<{}>",
                                   scheduledMessageId, channel, res.getError());
                           throw new SlackException("Failed to delete pending scheduled message;error:" +
                                                    res.getError());
                       }

                       logger.debug("Deleted pending scheduledMessage<{}> in channel<{}>",
                                    scheduledMessageId, channel);
                       return res;
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
                           logger.error(
                                   "Failed to get the permalink URL for messageTs<{}> in channel<{}>;error<{}>",
                                   messageTs, channel, res.getError());
                           return Mono.error(
                                   new SlackException("Failed to get the permalink;error:" + res.getError()));
                       }

                       return Mono.just(res.getPermalink());
                   });
    }

    /**
     * TBW.
     */
    public Mono<List<Message>> getThreadOfMessages(String channel, String ts) {
        return Mono.just(ConversationsRepliesRequest.builder()
                                                    .channel(requireNonNull(channel, "channel"))
                                                    .ts(requireNonNull(ts, "ts"))
                                                    .build())
                   .flatMap(req -> Mono.fromFuture(client.conversationsReplies(req)))
                   .flatMap(res -> {
                       if (!res.isOk()) {
                           logger.error(
                                   "Failed to get a thread of messages for ts<{}> in channel<{}>;error<{}>",
                                   ts, channel, res.getError());
                           return Mono.error(
                                   new SlackException(
                                           "Failed to get a thread of messages;error:" + res.getError()));
                       }

                       return Mono.just(res.getMessages());
                   });
    }
}
