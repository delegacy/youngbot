package com.github.delegacy.youngbot.server.slack;

import java.io.IOException;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.delegacy.youngbot.server.util.JacksonUtils;
import com.hubspot.slack.client.jackson.ObjectMapperUtils;
import com.hubspot.slack.client.models.events.ChallengeEvent;
import com.hubspot.slack.client.models.events.SlackEventWrapper;
import com.hubspot.slack.client.models.response.SlackError;
import com.hubspot.slack.client.models.response.SlackErrorResponse;
import com.hubspot.slack.client.models.response.SlackErrorType;
import com.hubspot.slack.client.models.response.SlackResponse;

final class SlackJacksonUtils {
    private static final Logger logger = LoggerFactory.getLogger(SlackJacksonUtils.class);

    private static final ObjectMapper OM = ObjectMapperUtils.mapper();

    static String serialize(Object obj) {
        return JacksonUtils.serialize(OM, obj);
    }

    static Object deserializeEvent(@Nullable String str) {
        if (str == null) {
            logger.warn("Failed to deserialize null");
            throw new IllegalArgumentException("Failed to deserialize null");
        }

        try {
            return OM.readValue(str, SlackEventWrapper.class);
        } catch (IOException e) {
            try {
                return OM.readValue(str, ChallengeEvent.class);
            } catch (IOException ignored) {
                // do nothing
            }

            logger.warn("Failed to deserialize the str<{}>", str);
            throw new IllegalArgumentException("Failed to deserialize the str");
        }
    }

    static <T extends SlackResponse> SlackResponse deserializeResponse(String responseBody,
                                                                       Class<T> responseClass) {

        try {
            return OM.readValue(responseBody, responseClass);
        } catch (IOException e) {
            try {
                return OM.readValue(responseBody, SlackErrorResponse.class);
            } catch (IOException ex) {
                logger.warn("Failed to deserialize responseBody<{}>", responseBody, ex);

                return SlackErrorResponse.builder()
                                         .setOk(false)
                                         .setError(
                                                 SlackError.builder()
                                                           .setType(SlackErrorType.UNKNOWN)
                                                           .setError("Failed to deserialize responseBody")
                                                           .build())
                                         .build();
            }
        }
    }

    private SlackJacksonUtils() {}
}
