package com.github.delegacy.youngbot.boot;

import java.util.HashSet;
import java.util.Set;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.github.delegacy.youngbot.event.EventProcessor;
import com.github.delegacy.youngbot.event.EventService;
import com.github.delegacy.youngbot.event.message.EchoProcessor;
import com.github.delegacy.youngbot.event.message.PingProcessor;

/**
 * TBW.
 */
@Configuration
@ConditionalOnWebApplication(type = Type.REACTIVE)
@EnableConfigurationProperties(YoungBotSettings.class)
@Import({ LineConfiguration.class, SlackConfiguration.class })
@ComponentScan("com.github.delegacy.youngbot")
public class YoungBotAutoConfiguration {
    /**
     * TBW.
     */
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Bean
    @ConditionalOnMissingBean
    public EventService eventService(Set<EventProcessor> processors) {
        final Set<EventProcessor> moreProcessors = new HashSet<>(processors);
        moreProcessors.add(new PingProcessor());
        moreProcessors.add(new EchoProcessor());
        return new EventService(moreProcessors);
    }
}
