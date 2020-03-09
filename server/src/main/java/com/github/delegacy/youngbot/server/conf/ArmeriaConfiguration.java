package com.github.delegacy.youngbot.server.conf;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.linecorp.armeria.client.ClientFactory;
import com.linecorp.armeria.client.circuitbreaker.CircuitBreakerHttpClientBuilder;
import com.linecorp.armeria.client.circuitbreaker.CircuitBreakerStrategy;
import com.linecorp.armeria.server.docs.DocService;
import com.linecorp.armeria.server.logging.AccessLogWriter;
import com.linecorp.armeria.server.logging.LoggingService;
import com.linecorp.armeria.spring.ArmeriaServerConfigurator;
import com.linecorp.armeria.spring.web.reactive.ArmeriaClientConfigurator;

@Configuration
public class ArmeriaConfiguration {
    @Bean
    public ArmeriaServerConfigurator armeriaServerConfigurator() {
        return builder -> builder.serviceUnder("/docs", new DocService())
                                 .decorator(LoggingService.newDecorator())
                                 .accessLogWriter(AccessLogWriter.combined(), false);
    }

    @Bean
    public ClientFactory clientFactory() {
        return ClientFactory.DEFAULT;
    }

    @Bean
    public ArmeriaClientConfigurator armeriaClientConfigurator(ClientFactory clientFactory) {
        return builder -> builder.factory(clientFactory)
                                 .decorator(new CircuitBreakerHttpClientBuilder(
                                         CircuitBreakerStrategy.onServerErrorStatus()).newDecorator());
    }
}
