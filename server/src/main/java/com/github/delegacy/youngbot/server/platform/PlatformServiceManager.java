package com.github.delegacy.youngbot.server.platform;

import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class PlatformServiceManager {
    private Map<Platform, PlatformService> services = Collections.emptyMap();

    private final ApplicationContext applicationContext;

    private static final PlatformService NOOP = new NoopPlatformService();

    @PostConstruct
    public void init() {
        services = applicationContext.getBeansOfType(PlatformService.class)
                                     .values().stream()
                                     .collect(Collectors.toUnmodifiableMap(
                                             PlatformService::platform, Function.identity()));
    }

    public PlatformService get(Platform platform) {
        return services.getOrDefault(platform, NOOP);
    }
}
