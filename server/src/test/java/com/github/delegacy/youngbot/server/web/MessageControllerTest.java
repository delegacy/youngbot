package com.github.delegacy.youngbot.server.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import javax.annotation.Resource;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import com.github.delegacy.youngbot.server.message.MessageRequest;
import com.github.delegacy.youngbot.server.message.MessageResponse;
import com.github.delegacy.youngbot.server.message.service.MessageService;

import reactor.core.publisher.Flux;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class MessageControllerTest {
    @Resource
    private WebTestClient webClient;

    @MockBean
    private MessageService messageService;

    @Test
    void testOnWebhook() {
        when(messageService.process(any())).thenReturn(
                Flux.just(MessageResponse.of(MessageRequest.of("text", "channel"), "PONG")));

        webClient.post().uri("/api/message/v1/webhook")
                 .contentType(MediaType.APPLICATION_JSON)
                 .body(BodyInserters.fromValue("{\"text\":\"ping\"}"))
                 .exchange()
                 .expectStatus().isOk()
                 .expectBody()
                 .json("[{\"text\":\"PONG\"}]");
    }

    @Test
    void testOnWebhook_multipleWebhookResponses() {
        when(messageService.process(any())).thenReturn(
                Flux.just(MessageResponse.of(MessageRequest.of("text", "channel"), "PONG"))
                    .repeat(2));

        webClient.post().uri("/api/message/v1/webhook")
                 .contentType(MediaType.APPLICATION_JSON)
                 .body(BodyInserters.fromValue("{\"text\":\"ping\"}"))
                 .exchange()
                 .expectStatus().isOk()
                 .expectBody()
                 .json("[{\"text\":\"PONG\"},{\"text\":\"PONG\"},{\"text\":\"PONG\"}]");
    }

    @Test
    void testOnWebhook_badRequestException() {
        webClient.post().uri("/api/message/v1/webhook")
                 .contentType(MediaType.APPLICATION_JSON)
                 .body(BodyInserters.fromValue("{}"))
                 .exchange()
                 .expectStatus().isBadRequest();
    }

    @Test
    void testOnWebhook_internalServerErrorException() {
        when(messageService.process(any())).thenThrow(RuntimeException.class);

        webClient.post().uri("/api/message/v1/webhook")
                 .contentType(MediaType.APPLICATION_JSON)
                 .body(BodyInserters.fromValue("{\"text\":\"ping\"}"))
                 .exchange()
                 .expectStatus().isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
