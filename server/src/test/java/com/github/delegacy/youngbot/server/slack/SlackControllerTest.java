package com.github.delegacy.youngbot.server.slack;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import javax.annotation.Resource;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import com.github.delegacy.youngbot.server.RequestContext;
import com.github.delegacy.youngbot.server.junit.TextFile;
import com.github.delegacy.youngbot.server.junit.TextFileParameterResolver;
import com.github.delegacy.youngbot.server.message.service.MessageService;
import com.github.delegacy.youngbot.server.platform.Platform;

@ExtendWith(SpringExtension.class)
@ExtendWith(TextFileParameterResolver.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class SlackControllerTest {
    @Resource
    private WebTestClient webClient;

    @MockBean
    private MessageService messageService;

    @Test
    void testSlackEventMessage(@TextFile("slackEventMessage.json") String json) {
        webClient.post().uri("/api/slack/v1/event")
                 .body(BodyInserters.fromObject(json))
                 .exchange()
                 .expectStatus().isOk();

        final ArgumentCaptor<RequestContext> reqCtxCaptor = ArgumentCaptor.forClass(RequestContext.class);
        final ArgumentCaptor<String> textCaptor = ArgumentCaptor.forClass(String.class);

        verify(messageService, times(1))
                .process(reqCtxCaptor.capture(), textCaptor.capture());

        final RequestContext ctx = reqCtxCaptor.getValue();
        final String text = textCaptor.getValue();
        assertThat(ctx.platform()).isEqualTo(Platform.SLACK);
        assertThat(ctx.replyTo()).isEqualTo("aChannel");
        assertThat(ctx.text()).isEqualTo("ping");
        assertThat(text).isEqualTo("ping");
    }

    @Test
    void testSlackEventBotMessage(@TextFile("slackEventBotMessage.json") String json) {
        webClient.post().uri("/api/slack/v1/event")
                 .body(BodyInserters.fromObject(json))
                 .exchange()
                 .expectStatus().isOk();

        verify(messageService, never()).process(any(), any());
    }

    @Test
    void testChallengeEvent(@TextFile("challengeEvent.json") String json) {
        webClient.post().uri("/api/slack/v1/event")
                 .body(BodyInserters.fromObject(json))
                 .exchange()
                 .expectStatus().isOk()
                 .expectBody(String.class)
                 .isEqualTo("{\"challenge\":\"3eZbrw1aBm2rZgRNFdxV2595E9CY3gmdALWMmHkvFXO7tYXAYM8P\"}");

        verify(messageService, never()).process(any(), any());
    }

    @Test
    void testInvalidRequestBody() {
        webClient.post().uri("/api/slack/v1/event")
                 .body(BodyInserters.fromObject("{}"))
                 .exchange()
                 .expectStatus().is5xxServerError();

        verify(messageService, never()).process(any(), any());
    }
}
