package com.github.delegacy.youngbot.slack;

import static java.util.Objects.requireNonNull;

import javax.annotation.PostConstruct;

import com.github.delegacy.youngbot.message.MessageService;
import com.github.delegacy.youngbot.slack.reaction.SlackReactionService;
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
    public SlackAppService(App app, MessageService messageService, SlackClient slackClient,
                           SlackReactionService slackReactionService) {
        blockingService =
                new SlackAppBlockingService(requireNonNull(app, "app"),
                                            requireNonNull(messageService, "messageService"),
                                            requireNonNull(slackClient, "slackClient"),
                                            requireNonNull(slackReactionService, "slackReactionService"));
    }

    /**
     * TBW.
     */
    @PostConstruct
    public void initialize() {
        blockingService.initialize();
    }

    /**
     * TBW.
     */
    @SuppressWarnings("rawtypes")
    public Mono<Response> run(Request request) {
        return Mono.fromCallable(() -> blockingService.run(request))
                   .subscribeOn(Schedulers.boundedElastic());
    }
}
