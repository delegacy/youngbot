package com.github.delegacy.youngbot.slack;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.annotation.Resource;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import com.github.delegacy.youngbot.internal.testing.TextFile;
import com.github.delegacy.youngbot.internal.testing.TextFileParameterResolver;
import com.slack.api.bolt.request.Request;
import com.slack.api.bolt.request.RequestType;
import com.slack.api.bolt.request.builtin.EventRequest;
import com.slack.api.bolt.response.Response;

import reactor.core.publisher.Mono;

@ExtendWith(SpringExtension.class)
@ExtendWith(TextFileParameterResolver.class)
@ContextConfiguration(classes = TestConfiguration.class)
@WebFluxTest(SlackController.class)
class AbstractSlackControllerTest {
    @Resource
    private WebTestClient webClient;

    @MockBean
    private SlackAppService slackAppService;

    @Test
    void testMessageEvent(@TextFile("slackEventMessage.json") String json) throws Exception {
        when(slackAppService.run(any(Request.class))).thenReturn(Mono.just(Response.ok()));

        webClient.post().uri("/api/slack/v1/webhook")
                 .body(BodyInserters.fromValue(json))
                 .exchange()
                 .expectStatus().isOk();

        final var captor = ArgumentCaptor.forClass(EventRequest.class);
        verify(slackAppService).run(captor.capture());

        final var req = captor.getValue();
        assertThat(req.getRequestType()).isEqualTo(RequestType.Event);
        assertThat(req.getRequestBodyAsString()).isEqualTo(json);
    }

    @Test
    void testInternalError(@TextFile("slackEventMessage.json") String json) throws Exception {
        when(slackAppService.run(any(Request.class))).thenReturn(Mono.error(new RuntimeException("oops")));

        webClient.post().uri("/api/slack/v1/webhook")
                 .body(BodyInserters.fromValue(json))
                 .exchange()
                 .expectStatus().is5xxServerError();
    }
}
