package com.github.delegacy.youngbot.server.slack;

import static com.github.delegacy.youngbot.server.ReactorContextFilter.REACTOR_CONTEXT_KEY;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

import com.slack.api.bolt.App;
import com.slack.api.bolt.request.Request;
import com.slack.api.bolt.request.RequestHeaders;
import com.slack.api.bolt.util.QueryStringParser;
import com.slack.api.bolt.util.SlackRequestParser;

import reactor.core.publisher.Mono;
import reactor.util.context.Context;

/**
 * TBW.
 */
@RestController
@RequestMapping("/api/slack/v1")
public class SlackController {
    private static final Logger logger = LoggerFactory.getLogger(SlackController.class);

    private final SlackAppService slackAppService;

    private final SlackRequestParser requestParser;

    /**
     * TBW.
     */
    public SlackController(App app, SlackAppService slackAppService) {
        this.slackAppService = requireNonNull(slackAppService, "slackAppService");

        requestParser = new SlackRequestParser(requireNonNull(app, "app").config());
    }

    /**
     * TBW.
     */
    @PostMapping("/event")
    public Mono<ResponseEntity<String>> onEvent(RequestEntity<String> request, ServerWebExchange exchange) {
        return slackAppService.run(buildSlackRequest(request, exchange))
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
                              })
                              .subscriberContext((Context) exchange.getRequiredAttribute(REACTOR_CONTEXT_KEY));
    }

    private Request<?> buildSlackRequest(RequestEntity<String> request, ServerWebExchange exchange) {
        final String requestBody = request.getBody();
        final RequestHeaders headers = new RequestHeaders(toHeaderMap(request.getHeaders()));

        final SlackRequestParser.HttpRequest rawRequest =
                SlackRequestParser.HttpRequest.builder()
                                              .requestUri(request.getUrl().getPath())
                                              .queryString(QueryStringParser.toMap(request.getUrl().getQuery()))
                                              .headers(headers)
                                              .requestBody(requestBody)
                                              .remoteAddress(toRemoteAddress(exchange.getRequest()))
                                              .build();

        return requestParser.parse(rawRequest);
    }

    private static Map<String, List<String>> toHeaderMap(HttpHeaders httpHeaders) {
        return httpHeaders.entrySet().stream()
                          .collect(Collectors.toMap(Map.Entry::getKey, Entry::getValue));
    }

    private static HttpHeaders toHttpHeaders(Map<String, List<String>> headerMap) {
        return new HttpHeaders(CollectionUtils.toMultiValueMap(headerMap));
    }

    @Nullable
    private static String toRemoteAddress(ServerHttpRequest serverHttpRequest) {
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
}
