package com.github.delegacy.youngbot.controller;

import javax.annotation.Resource;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class HomeControllerTest {
    @Resource
    private WebTestClient webClient;

    @Test
    public void ok() throws Exception {
        webClient.get().uri("/")
                 .exchange()
                 .expectStatus().isOk()
                 .expectBody(String.class).isEqualTo("OK, this is Young Bot.");
    }

    @Test
    public void healthCheck() throws Exception {
        webClient.get().uri("/actuator/health")
                 .exchange()
                 .expectStatus().isOk()
                 .expectBody(String.class).isEqualTo("{\"status\":\"UP\"}");
    }
}
