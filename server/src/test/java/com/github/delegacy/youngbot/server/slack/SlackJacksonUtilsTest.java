package com.github.delegacy.youngbot.server.slack;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.github.delegacy.youngbot.server.util.junit.TextFile;
import com.github.delegacy.youngbot.server.util.junit.TextFileParameterResolver;
import com.hubspot.slack.client.models.events.ChallengeEventIF;
import com.hubspot.slack.client.models.events.SlackEvent;
import com.hubspot.slack.client.models.events.SlackEventMessage;
import com.hubspot.slack.client.models.events.SlackEventWrapperIF;
import com.hubspot.slack.client.models.response.SlackResponse;
import com.hubspot.slack.client.models.response.chat.ChatPostMessageResponse;

@ExtendWith(TextFileParameterResolver.class)
class SlackJacksonUtilsTest {
    @Test
    void testDeserializeEvent_slackEventMessage(@TextFile("slackEventMessage.json") String json) {
        final Object deserialized = SlackJacksonUtils.deserializeEvent(json);
        assertThat(deserialized).isInstanceOf(SlackEventWrapperIF.class);

        @SuppressWarnings("rawtypes")
        final SlackEventWrapperIF slackEventWrapper = (SlackEventWrapperIF) deserialized;
        final SlackEvent slackEvent = slackEventWrapper.getEvent();
        assertThat(slackEvent).isInstanceOf(SlackEventMessage.class);

        final SlackEventMessage slackEventMessage = (SlackEventMessage) slackEvent;

        assertThat(slackEventMessage.getText()).isEqualTo("ping");
    }

    @Test
    void testDeserializeEvent_challengeEvent(@TextFile("challengeEvent.json") String json) {
        final Object deserialized = SlackJacksonUtils.deserializeEvent(json);
        assertThat(deserialized).isInstanceOf(ChallengeEventIF.class);

        final ChallengeEventIF challengeEvent = (ChallengeEventIF) deserialized;
        assertThat(challengeEvent.getChallenge())
                .isEqualTo("3eZbrw1aBm2rZgRNFdxV2595E9CY3gmdALWMmHkvFXO7tYXAYM8P");
    }

    @Test
    void testDeserializeEvent_invalidJson() {
        assertThatThrownBy(() -> SlackJacksonUtils.deserializeEvent("{}"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testDeserializeResponse_chatPostMessageResponse(
            @TextFile("chatPostMessageResponse.json") String json) {

        final SlackResponse res = SlackJacksonUtils.deserializeResponse(json, ChatPostMessageResponse.class);
        assertThat(res.isOk()).isTrue();
    }

    @Test
    void testDeserializeResponse_slackErrorResponse(@TextFile("slackErrorResponse.json") String json) {
        final SlackResponse res = SlackJacksonUtils.deserializeResponse(json, ChatPostMessageResponse.class);
        assertThat(res.isOk()).isFalse();
    }

    @Test
    void testDeserializeResponse_invalidJson() {
        final SlackResponse res = SlackJacksonUtils.deserializeResponse("{}", ChatPostMessageResponse.class);
        assertThat(res.isOk()).isFalse();
    }
}
