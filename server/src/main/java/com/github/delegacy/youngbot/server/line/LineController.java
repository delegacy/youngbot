package com.github.delegacy.youngbot.server.line;

import static com.github.delegacy.youngbot.server.ReactorContextFilter.REACTOR_CONTEXT_KEY;
import static java.util.Objects.requireNonNull;

import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

import com.github.delegacy.youngbot.server.message.MessageContext;
import com.github.delegacy.youngbot.server.message.service.MessageService;

import com.linecorp.bot.model.event.CallbackRequest;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.MessageContent;
import com.linecorp.bot.model.event.message.TextMessageContent;

import reactor.core.scheduler.Schedulers;
import reactor.util.context.Context;

@RestController
@RequestMapping("/api/line/v1")
public class LineController {
    private static final Logger logger = LoggerFactory.getLogger(LineController.class);

    private final LineSignatureValidator lineSignatureValidator;

    private final MessageService messageService;

    @Inject
    public LineController(LineSignatureValidator lineSignatureValidator,
                          MessageService messageService) {

        this.lineSignatureValidator = requireNonNull(lineSignatureValidator, "lineSignatureValidator");
        this.messageService = requireNonNull(messageService, "messageService");
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> onWebhook(RequestEntity<String> req, ServerWebExchange exchange) {
        final List<String> signatures = req.getHeaders().get("X-Line-Signature");
        if (CollectionUtils.isEmpty(signatures)) {
            logger.warn("No X-Line-Signature");
            return ResponseEntity.ok().build();
        }

        final String signature = signatures.get(0);
        final String payload = requireNonNull(req.getBody(), "payload");
        logger.debug("Received LINE webhook;signature<{}>,payload<{}>", signature, payload);

        final CallbackRequest callbackRequest = parsePayload(signature, payload);
        callbackRequest.getEvents().forEach(event -> {
            if (!(event instanceof MessageEvent)) {
                return;
            }

            @SuppressWarnings("rawtypes")
            final MessageEvent messageEvent = (MessageEvent) event;
            final MessageContent messageContent = messageEvent.getMessage();
            if (!(messageContent instanceof TextMessageContent)) {
                return;
            }

            final TextMessageContent textMessageContent = (TextMessageContent) messageContent;

            final MessageContext msgCtx = new LineMessageContext(textMessageContent.getText(),
                                                                 messageEvent.getReplyToken(),
                                                                 messageEvent.getSource().getSenderId());

            messageService.process(msgCtx)
                          .subscribeOn(Schedulers.elastic())
                          .subscriberContext((Context) exchange.getRequiredAttribute(REACTOR_CONTEXT_KEY))
                          .subscribe();
        });

        return ResponseEntity.ok().build();
    }

    private CallbackRequest parsePayload(String signature, String payload) {
        if (signature == null || signature.isEmpty()) {
            throw new IllegalArgumentException("No signature");
        }

        final byte[] json = payload.getBytes(StandardCharsets.UTF_8);
        if (!lineSignatureValidator.validateSignature(json, signature)) {
            throw new IllegalArgumentException("Invalid signature");
        }

        final CallbackRequest callbackRequest = LineJacksonUtils.deserialize(json, CallbackRequest.class);
        if (callbackRequest == null || callbackRequest.getEvents() == null) {
            throw new IllegalArgumentException("Invalid content");
        }

        return callbackRequest;
    }
}
