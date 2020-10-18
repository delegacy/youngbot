package com.github.delegacy.youngbot.line;

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

import com.linecorp.bot.model.event.CallbackRequest;
import com.linecorp.bot.parser.LineSignatureValidator;
import com.linecorp.bot.parser.WebhookParseException;
import com.linecorp.bot.parser.WebhookParser;

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
     *
     * @throws IOException TBW
     * @throws WebhookParseException TBW
     */
    @PostMapping("/webhook")
    public ResponseEntity<Void> onWebhook(RequestEntity<String> request)
            throws IOException, WebhookParseException {
        final CallbackRequest callback = buildCallbackRequest(request);
        lineService.handleCallback(callback)
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
