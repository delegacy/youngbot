package com.github.delegacy.youngbot.line;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import javax.annotation.Resource;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import com.github.delegacy.youngbot.internal.testing.TextFile;
import com.github.delegacy.youngbot.internal.testing.TextFileParameterResolver;

import com.linecorp.bot.parser.LineSignatureValidator;

import reactor.core.publisher.Mono;

@ExtendWith(SpringExtension.class)
@ExtendWith(TextFileParameterResolver.class)
@ContextConfiguration(classes = TestConfiguration.class)
@WebFluxTest(LineController.class)
class AbstractLineControllerIntegrationTest {
    @Resource
    private WebTestClient webClient;

    @Resource
    private LineSignatureValidator lineSignatureValidator;

    @Resource
    private LineClient lineClient;

    @Captor
    private ArgumentCaptor<List<String>> captor;

    @Test
    void testOnWebhook(@TextFile("messageEvent.json") String json) {
        when(lineSignatureValidator.validateSignature(any(), any())).thenReturn(true);
        when(lineClient.replyMessage(eq("replyToken"), any())).thenReturn(Mono.empty());

        webClient.post().uri("/api/line/v1/webhook")
                 .header("X-Line-Signature", "signature")
                 .body(BodyInserters.fromValue(json))
                 .exchange()
                 .expectStatus().isOk();

        await().untilAsserted(() -> {
            verify(lineClient).replyMessage(eq("replyToken"), captor.capture());
            assertThat(captor.getValue()).containsExactly("PONG");
        });
    }
}
