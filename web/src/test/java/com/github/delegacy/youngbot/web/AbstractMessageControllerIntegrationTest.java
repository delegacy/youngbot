package com.github.delegacy.youngbot.web;

import javax.annotation.Resource;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfiguration.class)
@WebFluxTest(MessageController.class)
class AbstractMessageControllerIntegrationTest {
    @Resource
    private WebTestClient webClient;

    @Test
    void testOnWebhook() {
        webClient.post().uri("/api/message/v1/webhook")
                 .contentType(MediaType.APPLICATION_JSON)
                 .body(BodyInserters.fromValue("{\"text\":\"ping\"}"))
                 .exchange()
                 .expectStatus().isOk()
                 .expectBody()
                 .json("[{\"text\":\"PONG\"}]");
    }
}
