package com.github.delegacy.youngbot.slack;

import static org.mockito.Mockito.mock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.delegacy.youngbot.event.EventService;
import com.github.delegacy.youngbot.internal.testing.AbstractTestConfiguration;
import com.slack.api.bolt.App;
import com.slack.api.bolt.AppConfig;

@Configuration
public class TestConfiguration extends AbstractTestConfiguration {
    @Bean
    public App app() {
        return new App(AppConfig.builder()
                                .signingSecret("signingSecret")
                                .build());
    }

    @Bean
    public SlackClient slackClient() {
        return mock(SlackClient.class);
    }

    @Bean
    public SlackService slackService(EventService eventService, SlackClient slackClient) {
        return new SlackService(eventService, slackClient);
    }

    @Bean
    public SlackAppService slackAppService(App app, SlackService slackService) {
        return new SlackAppService(app, slackService);
    }

    @Bean
    public SlackController slackController(App app, SlackAppService slackAppService) {
        return new SlackController(app, slackAppService);
    }
}
