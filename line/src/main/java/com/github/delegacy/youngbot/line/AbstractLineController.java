package com.github.delegacy.youngbot.line;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ServerWebInputException;

import com.linecorp.bot.model.event.CallbackRequest;
import com.linecorp.bot.parser.LineSignatureValidator;
import com.linecorp.bot.parser.WebhookParseException;
import com.linecorp.bot.parser.WebhookParser;

import reactor.core.publisher.Mono;

/**
 * TBW.
 */
public abstract class AbstractLineController {
    private static final Logger logger = LoggerFactory.getLogger(AbstractLineController.class);

    private final LineService lineService;

    private final WebhookParser webhookParser;

    /**
     * TBW.
     */
    protected AbstractLineController(LineService lineService, LineSignatureValidator lineSignatureValidator) {
        this.lineService = requireNonNull(lineService, "lineService");
        webhookParser = new WebhookParser(requireNonNull(lineSignatureValidator, "webhookParser"));
    }

    /**
     * TBW.
     */
    @PostMapping("${youngbot.line.webhookPath:/api/line/v1/webhook}")
    public void onWebhook(RequestEntity<String> request) {
        buildCallbackRequest(request)
                .map(lineService::handleCallback)
                .subscribe(null, t -> logger.error("Failed to handle callback", t));
    }

    private Mono<CallbackRequest> buildCallbackRequest(RequestEntity<String> request) {
        return Mono.fromSupplier(() -> {
            final List<String> signatures = request.getHeaders()
                                                   .getOrEmpty(WebhookParser.SIGNATURE_HEADER_NAME);
            if (signatures.isEmpty()) {
                throw new RuntimeException(new WebhookParseException("Missing 'X-Line-Signature' header"));
            }

            final String signature = signatures.get(0);
            final byte[] json = requireNonNull(request.getBody(), "payload").getBytes(StandardCharsets.UTF_8);
            try {
                return webhookParser.handle(signature, json);
            } catch (IOException | WebhookParseException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * TBW.
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({ ServerWebInputException.class, WebhookParseException.class })
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
