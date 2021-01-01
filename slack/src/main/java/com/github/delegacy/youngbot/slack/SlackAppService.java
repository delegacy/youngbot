package com.github.delegacy.youngbot.slack;

import static java.util.Objects.requireNonNull;

import javax.annotation.PostConstruct;

import com.slack.api.bolt.App;
import com.slack.api.bolt.request.Request;
import com.slack.api.bolt.response.Response;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * TBW.
 */
public class SlackAppService {
    private final SlackAppBlockingService blockingService;

    /**
     * TBW.
     */
    public SlackAppService(App app, SlackService slackService) {
        blockingService =
                new SlackAppBlockingService(requireNonNull(app, "app"),
                                            requireNonNull(slackService, "slackService"));
    }

    /**
     * TBW.
     */
    @PostConstruct
    public void init() {
        blockingService.init();
    }

    /**
     * TBW.
     */
    public Mono<Response> run(Request<?> request) {
        return Mono.fromCallable(() -> blockingService.run(request))
                   .subscribeOn(Schedulers.boundedElastic());
    }
}
