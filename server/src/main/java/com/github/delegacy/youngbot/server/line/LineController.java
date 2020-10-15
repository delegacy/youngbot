package com.github.delegacy.youngbot.server.line;

import static com.github.delegacy.youngbot.server.web.ReactorContextFilter.REACTOR_CONTEXT_KEY;
import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

import com.linecorp.bot.model.event.CallbackRequest;
import com.linecorp.bot.parser.WebhookParseException;
import com.linecorp.bot.parser.WebhookParser;

import reactor.util.context.Context;

/**
 * TBW.
 */
@RestController
@RequestMapping("/api/line/v1")
public class LineController {
    private static final Logger logger = LoggerFactory.getLogger(LineController.class);

    private final LineService lineService;

    private final WebhookParser webhookParser;

    /**
     * TBW.
     */
    public LineController(LineService lineService, WebhookParser webhookParser) {
        this.lineService = requireNonNull(lineService, "lineService");
        this.webhookParser = requireNonNull(webhookParser, "webhookParser");
    }

    /**
     * TBW.
     *
     * @throws IOException TBW
     * @throws WebhookParseException TBW
     */
    @PostMapping("/webhook")
    public ResponseEntity<Void> onWebhook(RequestEntity<String> request, ServerWebExchange exchange)
            throws IOException, WebhookParseException {

        final CallbackRequest callback = buildCallbackRequest(request);
        lineService.handleCallback(callback)
                   .subscriberContext((Context) exchange.getRequiredAttribute(REACTOR_CONTEXT_KEY))
                   .subscribe(null,
                              t -> logger.error("Failed to handle callback<{}>", callback, t));

        return ResponseEntity.ok().build();
    }

    private CallbackRequest buildCallbackRequest(RequestEntity<String> request)
            throws IOException, WebhookParseException {
        final List<String> signatures = request.getHeaders().getOrEmpty(WebhookParser.SIGNATURE_HEADER_NAME);
        if (signatures.isEmpty()) {
            throw new WebhookParseException("Missing 'X-Line-Signature' header");
        }

        final String signature = signatures.get(0);
        final byte[] json = requireNonNull(request.getBody(), "payload").getBytes(StandardCharsets.UTF_8);

        return webhookParser.handle(signature, json);
    }

    /**
     * TBW.
     */
    @ExceptionHandler
    public ResponseEntity<String> handleException(WebhookParseException e) {
        logger.debug("Failed to parse a webhook", e);

        return ResponseEntity.badRequest()
                             .contentType(MediaType.APPLICATION_JSON)
                             .body("{\"error\":\"" + e.getMessage() + "\"}");
    }

    /**
     * TBW.
     */
    @ExceptionHandler
    public ResponseEntity<String> handleException(Exception e) {
        logger.debug("Failed to handle a webhook", e);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                             .contentType(MediaType.APPLICATION_JSON)
                             .body("{\"error\":\"" + e.getMessage() + "\"}");
    }
}
