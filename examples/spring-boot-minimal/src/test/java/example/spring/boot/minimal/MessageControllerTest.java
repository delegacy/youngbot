package example.spring.boot.minimal;

import javax.annotation.Resource;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class MessageControllerTest {
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
