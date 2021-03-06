package com.github.delegacy.youngbot.line;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.UncheckedIOException;
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

import com.github.delegacy.youngbot.Consumers;

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
     */
    @PostMapping("${youngbot.line.webhook-path:/api/line/v1/webhook}")
    public void onWebhook(RequestEntity<String> request) {
        final var callback = buildCallbackRequest(request);
        lineService.handleCallback(callback)
                   .subscribe(Consumers.noop(),
                              t -> logger.error("Failed to handle callback<{}>", callback, t));
    }

    private CallbackRequest buildCallbackRequest(RequestEntity<String> request) {
        final List<String> signatures = request.getHeaders()
                                               .getOrEmpty(WebhookParser.SIGNATURE_HEADER_NAME);
        if (signatures.isEmpty()) {
            throw new UncheckedWebhookParseException(
                    new WebhookParseException("Missing 'X-Line-Signature' header"));
        }

        final String signature = signatures.get(0);

        if (!request.hasBody()) {
            throw new UncheckedWebhookParseException(new WebhookParseException("Missing body"));
        }
        @SuppressWarnings("ConstantConditions")
        final byte[] json = request.getBody().getBytes(StandardCharsets.UTF_8);
        try {
            return webhookParser.handle(signature, json);
        } catch (WebhookParseException e) {
            throw new UncheckedWebhookParseException(e);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * TBW.
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({ ServerWebInputException.class, UncheckedWebhookParseException.class })
    public void onBadRequestException() {}
}
