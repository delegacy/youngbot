package com.github.delegacy.youngbot.server.slack;

import static java.util.Objects.requireNonNull;

import org.springframework.stereotype.Service;

import com.slack.api.bolt.request.Request;
import com.slack.api.bolt.response.Response;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
class SlackAppService {
    private final SlackAppBlockingService slackAppBlockingService;

    SlackAppService(SlackAppBlockingService slackAppBlockingService) {
        this.slackAppBlockingService = requireNonNull(slackAppBlockingService, "slackAppBlockingService");
    }

    @SuppressWarnings("rawtypes")
    Mono<Response> run(Request request) {
        return Mono.fromCallable(() -> slackAppBlockingService.run(request))
                   .subscribeOn(Schedulers.boundedElastic());
    }
}
