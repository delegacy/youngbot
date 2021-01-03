package com.github.delegacy.youngbot.line;

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

import com.linecorp.bot.model.event.CallbackRequest;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.parser.LineSignatureValidator;

import reactor.core.publisher.Mono;

@ExtendWith(SpringExtension.class)
@ExtendWith(TextFileParameterResolver.class)
@ContextConfiguration(classes = TestConfiguration.class)
@WebFluxTest(LineController.class)
class AbstractLineControllerTest {
    @Resource
    private WebTestClient webClient;

    @Resource
    private LineSignatureValidator lineSignatureValidator;

    @MockBean
    private LineService lineService;

    @Test
    void testOnWebhook(@TextFile("messageEvent.json") String json) {
        when(lineSignatureValidator.validateSignature(any(), any())).thenReturn(true);
        when(lineService.handleCallback(any())).thenReturn(Mono.empty());

        webClient.post().uri("/api/line/v1/webhook")
                 .header("X-Line-Signature", "signature")
                 .body(BodyInserters.fromValue(json))
                 .exchange()
                 .expectStatus().isOk();

        final ArgumentCaptor<CallbackRequest> captor = ArgumentCaptor.forClass(CallbackRequest.class);
        verify(lineService).handleCallback(captor.capture());

        @SuppressWarnings("unchecked")
        final var event = (MessageEvent<TextMessageContent>) captor.getValue().getEvents().get(0);
        assertThat(event.getReplyToken()).isEqualTo("replyToken");
        assertThat(event.getMessage().getText()).isEqualTo("ping");
    }

    @Test
    void testOnWebhook_missingSignature(@TextFile("messageEvent.json") String json) {
        webClient.post().uri("/api/line/v1/webhook")
                 .body(BodyInserters.fromValue(json))
                 .exchange()
                 .expectStatus().isBadRequest();
    }

    @Test
    void testOnWebhook_badSignature(@TextFile("messageEvent.json") String json) {
        when(lineSignatureValidator.validateSignature(any(), any())).thenReturn(false);

        webClient.post().uri("/api/line/v1/webhook")
                 .header("X-Line-Signature", "badSignature")
                 .body(BodyInserters.fromValue(json))
                 .exchange()
                 .expectStatus().isBadRequest();
    }

    @Test
    void testOnWebhook_missingBody() {
        when(lineSignatureValidator.validateSignature(any(), any())).thenReturn(true);

        webClient.post().uri("/api/line/v1/webhook")
                 .header("X-Line-Signature", "signature")
                 .exchange()
                 .expectStatus().isBadRequest();
    }
}
