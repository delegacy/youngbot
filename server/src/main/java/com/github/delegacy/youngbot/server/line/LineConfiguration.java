package com.github.delegacy.youngbot.server.line;

import static java.util.Objects.requireNonNull;

import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.parser.LineSignatureValidator;
import com.linecorp.bot.parser.WebhookParser;

@Configuration
class LineConfiguration {
    @Bean
    LineSignatureValidator lineSignatureValidator(
            @Value("${youngbot.line.channel-secret}") String channelSecret) {
        return new LineSignatureValidator(requireNonNull(channelSecret, "channelSecret")
                                                  .getBytes(StandardCharsets.US_ASCII));
    }

    @Bean
    WebhookParser lineBotCallbackRequestParser(LineSignatureValidator lineSignatureValidator) {
        return new WebhookParser(lineSignatureValidator);
    }

    @Bean
    LineMessagingClient lineMessagingClient(@Value("${youngbot.line.channel-token}") String channelToken) {
        return LineMessagingClient.builder(requireNonNull(channelToken, "channelToken"))
                                  .build();
    }
}
