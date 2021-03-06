package com.github.delegacy.youngbot.web;

import static java.util.Objects.requireNonNull;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ServerWebInputException;

import com.github.delegacy.youngbot.event.EventService;
import com.github.delegacy.youngbot.event.message.MessageEvent;

import reactor.core.publisher.Mono;

/**
 * TBW.
 */
public abstract class AbstractMessageController {
    private static final Logger logger = LoggerFactory.getLogger(AbstractMessageController.class);

    private final EventService eventService;

    /**
     * TBW.
     */
    protected AbstractMessageController(EventService eventService) {
        this.eventService = requireNonNull(eventService, "eventService");
    }

    /**
     * TBW.
     */
    @PostMapping("${youngbot.webhook-path:/api/message/v1/webhook}")
    public Mono<List<WebhookResponse>> onWebhook(@RequestBody Mono<WebhookRequest> request) {
        return request.map(req -> MessageEvent.of(req.getText()))
                      .flatMapMany(eventService::process)
                      .map(WebhookResponse::new)
                      .collectList();
    }

    /**
     * TBW.
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ServerWebInputException.class)
    public void onBadRequestException() {}
}
