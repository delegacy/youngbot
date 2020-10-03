package com.github.delegacy.youngbot.server.line;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
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

import com.github.delegacy.youngbot.server.message.service.MessageService;
import com.github.delegacy.youngbot.server.platform.Platform;
import com.github.delegacy.youngbot.server.util.junit.TextFile;
import com.github.delegacy.youngbot.server.util.junit.TextFileParameterResolver;

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
    private MessageService messageService;

    @BeforeEach
    void beforeEach() {
        when(lineSignatureValidator.validateSignature(any(), any())).thenReturn(true);
        when(messageService.process(any())).thenReturn(Flux.empty());
    }

    @Test
    void testMessageEvent(@TextFile("messageEvent.json") String json) {
        webClient.post().uri("/api/line/v1/webhook")
                 .header("X-Line-Signature", "aSignature")
                 .body(BodyInserters.fromObject(json))
                 .exchange()
                 .expectStatus().isOk();

        final ArgumentCaptor<LineMessageContext> msgCtxCaptor =
                ArgumentCaptor.forClass(LineMessageContext.class);

        verify(messageService, times(1)).process(msgCtxCaptor.capture());

        final LineMessageContext msgCtx = msgCtxCaptor.getValue();
        assertThat(msgCtx.platform()).isEqualTo(Platform.LINE);
        assertThat(msgCtx.replyToken()).isEqualTo("aReplyToken");
        assertThat(msgCtx.text()).isEqualTo("ping");
    }
}
