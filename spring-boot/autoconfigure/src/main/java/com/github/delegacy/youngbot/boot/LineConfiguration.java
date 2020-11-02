package com.github.delegacy.youngbot.boot;

import static java.util.Objects.requireNonNull;

import java.nio.charset.StandardCharsets;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.delegacy.youngbot.boot.YoungBotSettings.Line;
import com.github.delegacy.youngbot.event.EventService;
import com.github.delegacy.youngbot.line.LineClient;
import com.github.delegacy.youngbot.line.LineService;

import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.parser.LineSignatureValidator;

/**
 * TBW.
 */
@Configuration
@ConditionalOnClass(LineService.class)
public class LineConfiguration {
    /**
     * TBW.
     */
    @Bean
    @ConditionalOnMissingBean
    public LineSignatureValidator lineSignatureValidator(YoungBotSettings youngBotSettings) {
        final Line line = requireNonNull(youngBotSettings.getLine(), "line");
        return new LineSignatureValidator(line.getChannelSecret().getBytes(StandardCharsets.US_ASCII));
    }

    /**
     * TBW.
     */
    @Bean
    @ConditionalOnMissingBean
    public LineMessagingClient lineMessagingClient(YoungBotSettings youngBotSettings) {
        final Line line = requireNonNull(youngBotSettings.getLine(), "line");
        return LineMessagingClient.builder(line.getChannelToken()).build();
    }

    /**
     * TBW.
     */
    @Bean
    @ConditionalOnMissingBean
    public LineClient lineClient(LineMessagingClient lineMessagingClient) {
        return new LineClient(lineMessagingClient);
    }

    /**
     * TBW.
     */
    @Bean
    @ConditionalOnMissingBean
    public LineService lineService(EventService eventService, LineClient lineClient) {
        return new LineService(eventService, lineClient);
    }
}
