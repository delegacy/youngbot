package com.github.delegacy.youngbot.server.slack;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import javax.annotation.Resource;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import com.github.delegacy.youngbot.server.RequestContext;
import com.github.delegacy.youngbot.server.message.service.MessageService;
import com.github.delegacy.youngbot.server.platform.Platform;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class SlackControllerTest {
    @Resource
    private WebTestClient webClient;

    @MockBean
    private MessageService messageService;

    @Test
    void testSlackEventMessage() throws Exception {
        webClient.post().uri("/api/slack/v1/event")
                 .body(BodyInserters.fromObject(
                         "{\"token\":\"aToken\",\"team_id\":\"teamId\",\"api_app_id\":\"apiAppId\",\"event\":{\"client_msg_id\":\"6f5994aa-6155-46d8-babb-413b70d07d6d\",\"type\":\"message\",\"text\":\"ping\",\"user\":\"aUser\",\"ts\":\"1558965076.000200\",\"channel\":\"aChannel\",\"event_ts\":\"1558965076.000200\",\"channel_type\":\"im\"},\"type\":\"event_callback\",\"event_id\":\"EvK0HLQSP6\",\"event_time\":1558965076,\"authed_users\":[\"authedUser\"]}"))
                 .exchange()
                 .expectStatus().isOk();

        final ArgumentCaptor<RequestContext> reqCtxCaptor = ArgumentCaptor.forClass(RequestContext.class);
        final ArgumentCaptor<String> textCaptor = ArgumentCaptor.forClass(String.class);

        verify(messageService, times(1))
                .process(reqCtxCaptor.capture(), textCaptor.capture());

        final RequestContext ctx = reqCtxCaptor.getValue();
        final String text = textCaptor.getValue();
        assertThat(ctx.platform()).isEqualTo(Platform.SLACK);
        assertThat(ctx.replyTo()).isEqualTo("aChannel");
        assertThat(ctx.text()).isEqualTo("ping");
        assertThat(text).isEqualTo("ping");
    }

    @Test
    void testSlackEventBotMessage() throws Exception {
        webClient.post().uri("/api/slack/v1/event")
                 .body(BodyInserters.fromObject(
                         "{\"token\":\"aToken\",\"team_id\":\"teamId\",\"api_app_id\":\"apiAppId\",\"event\":{\"type\":\"message\",\"subtype\":\"bot_message\",\"text\":\"PONG\",\"ts\":\"1558965077.000300\",\"username\":\"aUsername\",\"bot_id\":\"botId\",\"channel\":\"aChannel\",\"event_ts\":\"1558965077.000300\",\"channel_type\":\"im\"},\"type\":\"event_callback\",\"event_id\":\"EvK0HLRGAG\",\"event_time\":1558965077,\"authed_users\":[\"authedUser\"]}"))
                 .exchange()
                 .expectStatus().isOk();

        verify(messageService, never()).process(any(), any());
    }

    @Test
    void testChallengeEvent() throws Exception {
        webClient.post().uri("/api/slack/v1/event")
                 .body(BodyInserters.fromObject(
                         "{\"token\":\"token\",\"challenge\":\"3eZbrw1aBm2rZgRNFdxV2595E9CY3gmdALWMmHkvFXO7tYXAYM8P\",\"type\": \"url_verification\"}"))
                 .exchange()
                 .expectStatus().isOk()
                 .expectBody(String.class)
                 .isEqualTo("{\"challenge\":\"3eZbrw1aBm2rZgRNFdxV2595E9CY3gmdALWMmHkvFXO7tYXAYM8P\"}");

        verify(messageService, never()).process(any(), any());
    }
}
