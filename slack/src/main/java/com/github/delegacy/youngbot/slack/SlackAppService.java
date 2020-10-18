package com.github.delegacy.youngbot.slack;

import static java.util.Objects.requireNonNull;

import com.github.delegacy.youngbot.message.MessageService;
import com.google.common.annotations.VisibleForTesting;
import com.slack.api.bolt.App;
import com.slack.api.bolt.request.Request;
import com.slack.api.bolt.response.Response;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * TBW.
 */
public class SlackAppService {
    private final SlackAppBlockingService slackAppBlockingService;

    /**
     * TBW.
     */
    public SlackAppService(App app, MessageService messageService) {
        slackAppBlockingService = new SlackAppBlockingService(
                requireNonNull(app, "app"), requireNonNull(messageService, "messageService"));
    }

    @VisibleForTesting
    SlackAppService(SlackAppBlockingService slackAppBlockingService) {
        this.slackAppBlockingService = requireNonNull(slackAppBlockingService, "slackAppBlockingService");
    }

    /**
     * TBW.
     */
    @SuppressWarnings("rawtypes")
    public Mono<Response> run(Request request) {
        return Mono.fromCallable(() -> slackAppBlockingService.run(request))
                   .subscribeOn(Schedulers.boundedElastic());
    }
}
