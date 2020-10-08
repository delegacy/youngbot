package com.github.delegacy.youngbot.server.slack;

import static java.util.Objects.requireNonNull;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.slack.api.bolt.App;
import com.slack.api.bolt.AppConfig;

@Configuration
class SlackConfiguration {
    @Bean
    App app(@Value("${youngbot.slack.bot-token}") String botToken,
            @Value("${youngbot.slack.signing-secret}") String signingSecret) {
        final AppConfig appConfig = AppConfig.builder()
                                             .singleTeamBotToken(requireNonNull(botToken, "botToken"))
                                             .signingSecret(requireNonNull(signingSecret, "signingSecret"))
                                             .build();
        return new App(appConfig);
    }
}
