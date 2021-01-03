package com.github.delegacy.youngbot.slack;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.annotation.Resource;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import com.github.delegacy.youngbot.internal.testing.TextFile;
import com.github.delegacy.youngbot.internal.testing.TextFileParameterResolver;
import com.slack.api.app_backend.SlackSignature;
import com.slack.api.bolt.App;
import com.slack.api.model.Message;

import reactor.core.publisher.Mono;

@ExtendWith(SpringExtension.class)
@ExtendWith(TextFileParameterResolver.class)
@ContextConfiguration(classes = TestConfiguration.class)
@WebFluxTest(SlackController.class)
class AbstractSlackControllerIntegrationTest {
    @Resource
    private WebTestClient webClient;

    @MockBean
    private SlackClient slackClient;

    @Resource
    private App app;

    @Test
    void testMessageEvent(@TextFile("slackEventMessage.json") String json)
            throws Exception {
        final var message = new Message();
        message.setTs("ts");
        when(slackClient.postMessage(anyString(), anyString(), anyString())).thenReturn(Mono.just(message));

        final var generator = new SlackSignature.Generator(app.config().getSigningSecret());
        final var timestamp = String.valueOf(System.currentTimeMillis() / 1000);

        webClient.post().uri("/api/slack/v1/webhook")
                 .header("X-Slack-Request-Timestamp", timestamp)
                 .header("X-Slack-Signature", generator.generate(timestamp, json))
                 .body(BodyInserters.fromValue(json))
                 .exchange()
                 .expectStatus().isOk();

        await().untilAsserted(
                () -> verify(slackClient).postMessage(eq("channel"), eq("PONG"), eq("1558965076.000200")));
    }
}
