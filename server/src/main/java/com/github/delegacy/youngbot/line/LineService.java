package com.github.delegacy.youngbot.line;

import java.io.IOException;

import javax.inject.Inject;

import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.common.base.Preconditions;
import com.hubspot.slack.client.jackson.ObjectMapperUtils;

import com.linecorp.armeria.client.HttpClient;
import com.linecorp.armeria.common.AggregatedHttpMessage;
import com.linecorp.armeria.common.HttpData;
import com.linecorp.armeria.common.HttpHeaderNames;
import com.linecorp.armeria.common.HttpHeaders;
import com.linecorp.armeria.common.HttpMethod;
import com.linecorp.armeria.common.MediaType;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.message.TextMessage;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class LineService {
    private static String serialize(Object obj) {
        try {
            return ObjectMapperUtils.mapper().writeValueAsString(obj);
        } catch (IOException e) {
            log.warn("Failed to serialize the obj<{}>", obj, e);
            throw new IllegalArgumentException("Failed to serialize the obj");
        }
    }

    private final HttpClient lineClient;
    private final String authorization;

    @Inject
    public LineService(@Value("${youngbot.line.client.base-uri}") String apiBaseUri,
                       @Value("${youngbot.line.channel-token}") String channelToken) {

        Preconditions.checkArgument(Strings.isNotEmpty(apiBaseUri), "empty apiBaseUri");
        Preconditions.checkArgument(Strings.isNotEmpty(channelToken), "empty channelToken");

        lineClient = HttpClient.of(apiBaseUri);
        authorization = "Bearer " + channelToken;
    }

    public void replyMessage(String replyToken, String text) {
        final TextMessage textMessage = TextMessage.builder()
                                                   .text(text)
                                                   .build();

        final ReplyMessage replyMessage = new ReplyMessage(replyToken, textMessage);

        final AggregatedHttpMessage request =
                AggregatedHttpMessage.of(HttpHeaders.of(HttpMethod.POST, "/v2/bot/message/reply")
                                                    .set(HttpHeaderNames.AUTHORIZATION, authorization)
                                                    .setObject(HttpHeaderNames.CONTENT_TYPE, MediaType.JSON_UTF_8),
                                         HttpData.ofUtf8(serialize(replyMessage)));

        lineClient.execute(request).aggregate()
                  .whenComplete((res, cause) -> {
                      if (cause != null) {
                          log.warn("Failed to reply with replyToken<{}>", replyToken, cause);
                      } else {
                          log.info("Replied with replyToken<{}>;res<{}>",
                                   replyToken, res.content().toStringUtf8());
                      }
                  });
    }
}
