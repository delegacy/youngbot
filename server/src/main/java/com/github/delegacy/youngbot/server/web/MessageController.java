package com.github.delegacy.youngbot.server.web;

import static java.util.Objects.requireNonNull;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebInputException;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.delegacy.youngbot.server.message.MessageRequest;
import com.github.delegacy.youngbot.server.message.MessageResponse;
import com.github.delegacy.youngbot.server.message.service.MessageService;

import reactor.core.publisher.Mono;

/**
 * TBW.
 */
@RestController
@RequestMapping("/api/message/v1")
public class MessageController {
    private static final Logger logger = LoggerFactory.getLogger(MessageController.class);

    /**
     * TBW.
     */
    public static final class WebhookRequest {
        private final String text;

        /**
         * TBW.
         */
        @JsonCreator
        public WebhookRequest(@JsonProperty("text") String text) {
            this.text = requireNonNull(text, "text");
        }

        /**
         * TBW.
         */
        public String getText() {
            return text;
        }
    }

    /**
     * TBW.
     */
    public static final class WebhookResponse {
        private final String text;

        /**
         * TBW.
         */
        public WebhookResponse(MessageResponse messageResponse) {
            text = messageResponse.text();
        }

        /**
         * TBW.
         */
        public String getText() {
            return text;
        }
    }

    private final MessageService messageService;

    /**
     * TBW.
     */
    public MessageController(MessageService messageService) {
        this.messageService = requireNonNull(messageService, "messageService");
    }

    /**
     * TBW.
     */
    @PostMapping("/webhook")
    public Mono<List<WebhookResponse>> onWebhook(@RequestBody Mono<WebhookRequest> request,
                                                 ServerWebExchange exchange) {
        return request.map(req -> MessageRequest.of(req.getText(), exchange.getRequest().getId()))
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
