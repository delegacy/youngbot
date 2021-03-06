package com.github.delegacy.youngbot.slack;

import static java.util.Objects.requireNonNull;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ServerWebExchange;

import com.google.common.annotations.VisibleForTesting;
import com.slack.api.bolt.App;
import com.slack.api.bolt.request.Request;
import com.slack.api.bolt.request.RequestHeaders;
import com.slack.api.bolt.util.QueryStringParser;
import com.slack.api.bolt.util.SlackRequestParser;

import reactor.core.publisher.Mono;

/**
 * TBW.
 */
public abstract class AbstractSlackController {
    private static final Logger logger = LoggerFactory.getLogger(AbstractSlackController.class);

    private static Map<String, List<String>> toHeaderMap(HttpHeaders httpHeaders) {
        return httpHeaders.entrySet().stream()
                          .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }

    private static HttpHeaders toHttpHeaders(Map<String, List<String>> headerMap) {
        return new HttpHeaders(CollectionUtils.toMultiValueMap(headerMap));
    }

    @VisibleForTesting
    @Nullable
    static String toRemoteAddress(ServerHttpRequest serverHttpRequest) {
        final InetSocketAddress inetSocketAddress = serverHttpRequest.getRemoteAddress();
        if (inetSocketAddress == null) {
            return null;
        }
        final InetAddress inetAddress = inetSocketAddress.getAddress();
        if (inetAddress == null) {
            return null;
        }
        return inetAddress.getHostAddress();
    }

    private final SlackAppService slackAppService;

    private final SlackRequestParser requestParser;

    /**
     * TBW.
     */
    protected AbstractSlackController(App app, SlackAppService slackAppService) {
        this.slackAppService = requireNonNull(slackAppService, "slackAppService");

        requestParser = new SlackRequestParser(requireNonNull(app, "app").config());
    }

    /**
     * TBW.
     */
    @PostMapping("${youngbot.slack.webhook-path:/api/slack/v1/webhook}")
    public Mono<ResponseEntity<String>> onWebhook(RequestEntity<String> request, ServerWebExchange exchange) {
        return buildSlackRequest(request, exchange)
                .flatMap(slackAppService::run)
                .map(res -> {
                    final HttpHeaders resHeaders = toHttpHeaders(res.getHeaders());
                    resHeaders.add(HttpHeaders.CONTENT_TYPE, res.getContentType());

                    return ResponseEntity.status(res.getStatusCode())
                                         .headers(resHeaders)
                                         .body(res.getBody());
                })
                .onErrorResume(t -> {
                    logger.error("Failed to handle request<{}>", request, t);

                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                                   .contentType(MediaType.APPLICATION_JSON)
                                                   .body("{\"error\":\"Something is wrong.\"}"));
                });
    }

    private Mono<Request<?>> buildSlackRequest(RequestEntity<String> request, ServerWebExchange exchange) {
        return Mono.fromSupplier(() -> {
            final String requestBody = request.getBody();
            final RequestHeaders headers = new RequestHeaders(toHeaderMap(request.getHeaders()));

            final SlackRequestParser.HttpRequest rawRequest =
                    SlackRequestParser.HttpRequest.builder()
                                                  .requestUri(request.getUrl().getPath())
                                                  .queryString(QueryStringParser.toMap(
                                                          request.getUrl().getQuery()))
                                                  .headers(headers)
                                                  .requestBody(requestBody)
                                                  .remoteAddress(toRemoteAddress(exchange.getRequest()))
                                                  .build();

            return requestParser.parse(rawRequest);
        });
    }
}
