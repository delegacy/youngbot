package com.github.delegacy.youngbot.line;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.annotation.Resource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.BodyInserters;

import com.github.delegacy.youngbot.internal.testing.TestConfiguration;
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
@WebFluxTest(AbstractLineControllerTest.LineController.class)
class AbstractLineControllerTest {
    @RestController
    static class LineController extends AbstractLineController {
        @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
        protected LineController(LineService lineService, LineSignatureValidator lineSignatureValidator) {
            super(lineService, lineSignatureValidator);
        }
    }

    @Resource
    private WebTestClient webClient;

    @MockBean
    private LineSignatureValidator lineSignatureValidator;

    @MockBean
    private LineService lineService;

    @BeforeEach
    void beforeEach() {
        when(lineSignatureValidator.validateSignature(any(), any())).thenReturn(true);
        when(lineService.handleCallback(any())).thenReturn(Mono.empty());
    }

    @Test
    void testMessageEvent(@TextFile("messageEvent.json") String json) {
        webClient.post().uri("/api/line/v1/webhook")
                 .header("X-Line-Signature", "signature")
                 .body(BodyInserters.fromValue(json))
                 .exchange()
                 .expectStatus().isOk();

        final ArgumentCaptor<CallbackRequest> callbackCaptor =
                ArgumentCaptor.forClass(CallbackRequest.class);
        verify(lineService).handleCallback(callbackCaptor.capture());

        @SuppressWarnings("unchecked")
        final MessageEvent<TextMessageContent> event =
                (MessageEvent<TextMessageContent>) callbackCaptor.getValue().getEvents().get(0);
        assertThat(event.getReplyToken()).isEqualTo("replyToken1");
        assertThat(event.getMessage().getText()).isEqualTo("ping");
    }
}
