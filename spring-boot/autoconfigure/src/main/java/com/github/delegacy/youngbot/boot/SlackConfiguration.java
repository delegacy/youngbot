package com.github.delegacy.youngbot.boot;

import static java.util.Objects.requireNonNull;

import java.util.Set;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.delegacy.youngbot.boot.YoungBotSettings.Slack;
import com.github.delegacy.youngbot.message.MessageService;
import com.github.delegacy.youngbot.slack.SlackAppService;
import com.github.delegacy.youngbot.slack.SlackClient;
import com.github.delegacy.youngbot.slack.SlackRtmService;
import com.github.delegacy.youngbot.slack.reaction.SlackReactionService;
import com.github.delegacy.youngbot.slack.reaction.processor.SlackReactionProcessor;
import com.slack.api.bolt.App;
import com.slack.api.bolt.AppConfig;
import com.slack.api.rtm.RTMClient;

/**
 * TBW.
 */
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
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
    public SlackClient slackClient(App app) {
        return new SlackClient(app.slack().methodsAsync(app.config().getSingleTeamBotToken()));
    }

    /**
     * TBW.
     */
    @Bean
    @ConditionalOnMissingBean
    public SlackAppService slackAppService(App app, MessageService messageService, SlackClient slackClient,
                                           SlackReactionService slackReactionService) {
        return new SlackAppService(app, messageService, slackClient, slackReactionService);
    }

    /**
     * TBW.
     *
     * @throws Exception TBW
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(value = "youngbot.slack.rtm.enabled", havingValue = "true")
    public RTMClient slackRtmClient(App app) throws Exception {
        return app.slack().rtmConnect(app.config().getSingleTeamBotToken());
    }

    /**
     * TBW.
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(RTMClient.class)
    public SlackRtmService slackRtmService(
            RTMClient slackRtmClient, MessageService messageService, SlackClient slackClient,
            SlackReactionService slackReactionService) {
        return new SlackRtmService(slackRtmClient, messageService, slackClient, slackReactionService);
    }

    /**
     * TBW.
     */
    @Bean
    @ConditionalOnMissingBean
    public SlackReactionService slackReactionService(Set<SlackReactionProcessor> processors) {
        return new SlackReactionService(processors);
    }
}
