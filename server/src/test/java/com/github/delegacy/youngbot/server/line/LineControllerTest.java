package com.github.delegacy.youngbot.server.line;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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

import com.linecorp.bot.model.event.CallbackRequest;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.parser.LineSignatureValidator;

import reactor.core.publisher.Flux;

@ExtendWith(SpringExtension.class)
@ExtendWith(TextFileParameterResolver.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class LineControllerTest {
    @Resource
    private WebTestClient webClient;

    @MockBean
    private LineSignatureValidator lineSignatureValidator;

    @MockBean
    private LineService lineService;

    @BeforeEach
    void beforeEach() {
        when(lineSignatureValidator.validateSignature(any(), any())).thenReturn(true);
        when(lineService.handleCallback(any())).thenReturn(Flux.empty());
    }

    @Test
    void testMessageEvent(@TextFile("messageEvent.json") String json) {
        webClient.post().uri("/api/line/v1/webhook")
                 .header("X-Line-Signature", "aSignature")
                 .body(BodyInserters.fromValue(json))
                 .exchange()
                 .expectStatus().isOk();

        final ArgumentCaptor<CallbackRequest> callbackCaptor =
                ArgumentCaptor.forClass(CallbackRequest.class);

        verify(lineService).handleCallback(callbackCaptor.capture());

        @SuppressWarnings("unchecked")
        final MessageEvent<TextMessageContent> event =
                (MessageEvent<TextMessageContent>) callbackCaptor.getValue().getEvents().get(0);
        assertThat(event.getReplyToken()).isEqualTo("aReplyToken");
        assertThat(event.getMessage().getText()).isEqualTo("ping");
    }
}
