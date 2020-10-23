package com.github.delegacy.youngbot.slack;

import static java.util.Objects.requireNonNull;

import java.util.List;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.slack.api.methods.AsyncMethodsClient;
import com.slack.api.methods.request.chat.ChatGetPermalinkRequest;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import com.slack.api.methods.request.conversations.ConversationsRepliesRequest;
import com.slack.api.methods.response.chat.ChatGetPermalinkResponse;
import com.slack.api.methods.response.conversations.ConversationsRepliesResponse;
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
    public Mono<Void> sendMessage(String channel, String text) {
        return sendMessage(channel, text, null);
    }

    /**
     * TBW.
     */
    public Mono<Void> sendMessage(String channel, String text, @Nullable String threadTs) {
        final ChatPostMessageRequest req =
                ChatPostMessageRequest.builder()
                                      .channel(requireNonNull(channel, "channel"))
                                      .text(requireNonNull(text, "text"))
                                      .threadTs(threadTs)
                                      .build();

        return Mono.fromFuture(client.chatPostMessage(req))
                   .doOnNext(res -> {
                       if (res.isOk()) {
                           if (res.getWarning() != null) {
                               logger.warn("Sent text<{}> to channel<{}>;warn<{}>",
                                           text, channel, res.getWarning());
                           } else {
                               logger.debug("Sent text<{}> to channel<{}>", text, channel);
                           }
                       } else {
                           logger.error("Failed to send text<{}> to channel<{}>;error<{}>",
                                        text, channel, res.getError());
                       }
                   })
                   .then();
    }

    /**
     * TBW.
     */
    public Mono<String> getPermalink(String channel, String messageTs) {
        final ChatGetPermalinkRequest req =
                ChatGetPermalinkRequest.builder()
                                       .channel(requireNonNull(channel, "channel"))
                                       .messageTs(requireNonNull(messageTs, "messageTs"))
                                       .build();

        return Mono.fromFuture(client.chatGetPermalink(req))
                   .doOnNext(res -> {
                       if (!res.isOk()) {
                           logger.warn("Failed to get a permalink for messageTs<{}> in channel<{}>;error<{}>",
                                       messageTs, channel, res.getError());
                       }
                   })
                   .map(ChatGetPermalinkResponse::getPermalink);
    }

    /**
     * TBW.
     */
    public Mono<List<Message>> getThreadedMessages(String channel, String messageTs) {
        final ConversationsRepliesRequest req =
                ConversationsRepliesRequest.builder()
                                           .channel(channel)
                                           .ts(messageTs)
                                           .build();

        return Mono.fromFuture(client.conversationsReplies(req))
                   .doOnNext(res -> {
                       if (!res.isOk()) {
                           logger.warn("Failed to get a thread of messages for messageTs<{}> in channel<{}>;" +
                                       "error<{}>", messageTs, channel, res.getError());
                       }
                   })
                   .map(ConversationsRepliesResponse::getMessages);
    }
}
