package com.github.delegacy.youngbot.boot;

import static java.util.Objects.requireNonNull;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.delegacy.youngbot.boot.YoungBotSettings.Slack;
import com.github.delegacy.youngbot.message.MessageService;
import com.github.delegacy.youngbot.slack.SlackAppService;
import com.github.delegacy.youngbot.slack.SlackRtmService;
import com.slack.api.bolt.App;
import com.slack.api.bolt.AppConfig;

/**
 * TBW.
 */
@SuppressWarnings({ "SpringFacetCodeInspection", "SpringJavaInjectionPointsAutowiringInspection" })
@Configuration
@ConditionalOnClass(SlackAppService.class)
public class SlackConfiguration {
    /**
     * TBW.
     */
    @Bean
    @ConditionalOnMissingBean
    public App app(YoungBotSettings youngBotSettings) {
        final Slack slack = requireNonNull(youngBotSettings.getSlack(), "slack");
        final AppConfig appConfig = AppConfig.builder()
                                             .singleTeamBotToken(slack.getBotToken())
                                             .signingSecret(slack.getSigningSecret())
                                             .build();
        return new App(appConfig);
    }

    /**
     * TBW.
     */
    @Bean
    @ConditionalOnMissingBean
    public SlackAppService slackAppService(App app, MessageService messageService) {
        return new SlackAppService(app, messageService);
    }

    /**
     * TBW.
     *
     * @throws Exception TBW
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(value = "youngbot.slack.rtm.enabled", havingValue = "true")
    public SlackRtmService slackRtmService(App app, MessageService messageService) throws Exception {
        return new SlackRtmService(app, messageService);
    }
}
