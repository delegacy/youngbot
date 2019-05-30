package com.github.delegacy.youngbot.server.platform;

import static java.util.Objects.requireNonNull;

import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@SuppressWarnings("ClassWithOnlyPrivateConstructors")
@Component
public class PlatformServiceManager {
    private static final PlatformService NOOP = new NoopPlatformService();

    private final ApplicationContext applicationContext;

    private Map<Platform, PlatformService> services = Collections.emptyMap();

    private PlatformServiceManager(ApplicationContext applicationContext) {
        this.applicationContext = requireNonNull(applicationContext, "applicationContext");
    }

    @PostConstruct
    private void init() {
        services = applicationContext.getBeansOfType(PlatformService.class)
                                     .values().stream()
                                     .collect(Collectors.toUnmodifiableMap(
                                             PlatformService::platform, Function.identity()));
    }

    public PlatformService get(Platform platform) {
        return services.getOrDefault(platform, NOOP);
    }
}
