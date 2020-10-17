package com.github.delegacy.youngbot.boot;

import static java.util.Objects.requireNonNull;

import java.nio.charset.StandardCharsets;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.delegacy.youngbot.boot.YoungBotSettings.Line;
import com.github.delegacy.youngbot.line.LineService;
import com.github.delegacy.youngbot.message.MessageService;

import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.parser.LineSignatureValidator;

/**
 * TBW.
 */
@SuppressWarnings({ "SpringFacetCodeInspection", "SpringJavaInjectionPointsAutowiringInspection" })
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
        return LineMessagingClient.builder(line.getChannelToken())
                                  .build();
    }

    /**
     * TBW.
     */
    @Bean
    @ConditionalOnMissingBean
    public LineService lineService(LineMessagingClient lineMessagingClient, MessageService messageService) {
        return new LineService(lineMessagingClient, messageService);
    }
}
