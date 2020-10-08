package com.github.delegacy.youngbot.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import reactor.core.scheduler.Schedulers;

/**
 * TBW.
 */
@SuppressWarnings("checkstyle:HideUtilityClassConstructor")
@SpringBootApplication
public class Application {
    /**
     * TBW.
     */
    @SuppressWarnings("checkstyle:UncommentedMain")
    public static void main(String[] args) {
        Schedulers.enableMetrics();

        SpringApplication.run(Application.class, args);
    }
}
