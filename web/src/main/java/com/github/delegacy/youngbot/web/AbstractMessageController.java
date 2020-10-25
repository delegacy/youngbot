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
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebInputException;

import com.github.delegacy.youngbot.message.MessageRequest;
import com.github.delegacy.youngbot.message.MessageService;

import reactor.core.publisher.Mono;

/**
 * TBW.
 */
public abstract class AbstractMessageController {
    private static final Logger logger = LoggerFactory.getLogger(AbstractMessageController.class);

    private final MessageService messageService;

    /**
     * TBW.
     */
    protected AbstractMessageController(MessageService messageService) {
        this.messageService = requireNonNull(messageService, "messageService");
    }

    /**
     * TBW.
     */
    @PostMapping("${youngbot.webhookPath:/api/message/v1/webhook}")
    public Mono<List<WebhookResponse>> onWebhook(@RequestBody Mono<WebhookRequest> request,
                                                 ServerWebExchange exchange) {
        return request.map(req -> MessageRequest.of(exchange.getRequest().getId(), req.getText()))
                      .flatMapMany(messageService::process)
                      .map(WebhookResponse::new)
                      .collectList();
    }

    /**
     * TBW.
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ServerWebInputException.class)
    public void onBadRequestException(Throwable t) {
        logger.debug("Failed to handle a request", t);
    }

    /**
     * TBW.
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public void onInternalServerErrorException(Throwable t) {
        logger.error("Failed to handle a request", t);
    }
}
