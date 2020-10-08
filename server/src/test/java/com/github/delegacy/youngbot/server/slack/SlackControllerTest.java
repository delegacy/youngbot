package com.github.delegacy.youngbot.server.slack;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.annotation.Resource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import com.github.delegacy.youngbot.server.util.junit.TextFile;
import com.github.delegacy.youngbot.server.util.junit.TextFileParameterResolver;
import com.slack.api.bolt.request.Request;
import com.slack.api.bolt.request.RequestType;
import com.slack.api.bolt.request.builtin.EventRequest;
import com.slack.api.bolt.response.Response;

@ExtendWith(SpringExtension.class)
@ExtendWith(TextFileParameterResolver.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class SlackControllerTest {
    @Resource
    private WebTestClient webClient;

    @MockBean
    private SlackAppBlockingService slackAppBlockingService;

    @BeforeEach
    void beforeEach() throws Exception {
        when(slackAppBlockingService.run(any(Request.class))).thenReturn(Response.ok());
    }

    @Test
    void testMessageEvent(@TextFile("slackEventMessage.json") String json) throws Exception {
        webClient.post().uri("/api/slack/v1/event")
                 .body(BodyInserters.fromValue(json))
                 .exchange()
                 .expectStatus().isOk();

        final ArgumentCaptor<EventRequest> reqCaptor = ArgumentCaptor.forClass(EventRequest.class);
        verify(slackAppBlockingService, times(1)).run(reqCaptor.capture());

        final EventRequest req = reqCaptor.getValue();
        assertThat(req.getRequestType()).isEqualTo(RequestType.Event);
        assertThat(req.getRequestBodyAsString()).isEqualTo(json);
    }

    @Test
    void testInternalError(@TextFile("slackEventMessage.json") String json) throws Exception {
        when(slackAppBlockingService.run(any(Request.class))).thenThrow(RuntimeException.class);

        webClient.post().uri("/api/slack/v1/event")
                 .body(BodyInserters.fromValue(json))
                 .exchange()
                 .expectStatus().is5xxServerError();
    }
}
