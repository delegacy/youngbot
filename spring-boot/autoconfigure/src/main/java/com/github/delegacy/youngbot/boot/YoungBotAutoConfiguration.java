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

import com.github.delegacy.youngbot.message.MessageService;
import com.github.delegacy.youngbot.message.processor.EchoMessageProcessor;
import com.github.delegacy.youngbot.message.processor.MessageProcessor;
import com.github.delegacy.youngbot.message.processor.PingMessageProcessor;

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
    public MessageService messageService(Set<MessageProcessor> processors) {
        final Set<MessageProcessor> moreProcessors = new HashSet<>(processors);
        moreProcessors.add(new PingMessageProcessor());
        moreProcessors.add(new EchoMessageProcessor());
        return new MessageService(moreProcessors);
    }
}
