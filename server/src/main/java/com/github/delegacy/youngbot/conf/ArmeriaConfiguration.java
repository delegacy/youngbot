package com.github.delegacy.youngbot.conf;

import javax.annotation.Resource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.delegacy.youngbot.service.HelloService;

import com.linecorp.armeria.client.ClientFactory;
import com.linecorp.armeria.client.circuitbreaker.CircuitBreakerHttpClientBuilder;
import com.linecorp.armeria.client.circuitbreaker.CircuitBreakerStrategy;
import com.linecorp.armeria.common.grpc.GrpcSerializationFormats;
import com.linecorp.armeria.server.docs.DocService;
import com.linecorp.armeria.server.grpc.GrpcServiceBuilder;
import com.linecorp.armeria.server.logging.AccessLogWriter;
import com.linecorp.armeria.server.logging.LoggingService;
import com.linecorp.armeria.spring.ArmeriaServerConfigurator;
import com.linecorp.armeria.spring.web.reactive.ArmeriaClientConfigurator;

@Configuration
public class ArmeriaConfiguration {
    @Resource
    private HelloService helloService;

    @Bean
    public ArmeriaServerConfigurator armeriaServerConfigurator() {
        return builder -> {
            builder.serviceUnder("/docs", new DocService());

            builder.decorator(LoggingService.newDecorator());

            builder.accessLogWriter(AccessLogWriter.combined(), false);

            builder.service(
                    new GrpcServiceBuilder().addService(helloService)
                                            .supportedSerializationFormats(GrpcSerializationFormats.values())
                                            .enableUnframedRequests(true)
                                            .build());
        };
    }

    @Bean
    public ClientFactory clientFactory() {
        return ClientFactory.DEFAULT;
    }

    @Bean
    public ArmeriaClientConfigurator armeriaClientConfigurator(ClientFactory clientFactory) {
        return builder -> {
            final CircuitBreakerStrategy strategy = CircuitBreakerStrategy.onServerErrorStatus();
            builder.decorator(new CircuitBreakerHttpClientBuilder(strategy).newDecorator());

            builder.factory(clientFactory);
        };
    }
}
