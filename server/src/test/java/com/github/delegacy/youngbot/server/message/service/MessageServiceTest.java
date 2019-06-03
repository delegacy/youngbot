package com.github.delegacy.youngbot.server.message.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ServerWebExchange;

import com.github.delegacy.youngbot.server.RequestContext;
import com.github.delegacy.youngbot.server.TheVoid;
import com.github.delegacy.youngbot.server.message.handler.MessageHandler;
import com.github.delegacy.youngbot.server.message.handler.MessageHandlerManager;
import com.github.delegacy.youngbot.server.platform.Platform;
import com.github.delegacy.youngbot.server.platform.PlatformService;
import com.github.delegacy.youngbot.server.platform.PlatformServiceManager;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {
    private static class EchoDigitMessageHandler implements MessageHandler {
        @Override
        public Pattern pattern() {
            return Pattern.compile("\\d+");
        }

        @Override
        public Flux<String> process(RequestContext ctx, Matcher matcher) {
            return Flux.error(new IllegalStateException("oops"));
        }
    }

    private static class EchoWordMessageHandler implements MessageHandler {
        @Override
        public Pattern pattern() {
            return Pattern.compile("\\w+");
        }

        @Override
        public Flux<String> process(RequestContext ctx, Matcher matcher) {
            return Flux.just(matcher.group(0));
        }
    }

    private static class EchoAllTwiceMessageHandler implements MessageHandler {
        @Override
        public Pattern pattern() {
            return Pattern.compile(".+");
        }

        @Override
        public Flux<String> process(RequestContext ctx, Matcher matcher) {
            return Flux.just(matcher.group(0).repeat(2));
        }
    }

    @Mock
    private MessageHandlerManager messageHandlerManager;

    @Mock
    private PlatformServiceManager platformServiceManager;

    @Mock
    private ServerWebExchange exchange;

    @Mock
    private PlatformService platformService;

    @InjectMocks
    private MessageService messageService;

    @BeforeEach
    void beforeEach() {
        when(messageHandlerManager.handlers()).thenReturn(
                List.of(new EchoDigitMessageHandler(),
                        new EchoWordMessageHandler(),
                        new EchoAllTwiceMessageHandler()));
    }

    @Test
    void testProcess() {
        when(platformService.replyMessage(anyString(), anyString())).thenReturn(Mono.just(TheVoid.INSTANCE));
        when(platformServiceManager.get(eq(Platform.LINE))).thenReturn(platformService);

        final RequestContext ctx = new RequestContext(Platform.LINE, exchange,
                                                      "abc", "aReplyTo");
        StepVerifier.create(messageService.process(ctx))
                    .expectNextCount(2)
                    .expectComplete()
                    .verify();

        final ArgumentCaptor<String> toCaptor = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<String> textCaptor = ArgumentCaptor.forClass(String.class);

        verify(platformService, times(2))
                .replyMessage(toCaptor.capture(), textCaptor.capture());
        assertThat(toCaptor.getAllValues()).containsExactly("aReplyTo", "aReplyTo");
        assertThat(textCaptor.getAllValues()).containsExactly("abc", "abcabc");
    }

    @Test
    void testProcess_whenMessageHandlerThrows() {
        final RequestContext ctx = new RequestContext(Platform.LINE, exchange,
                                                      "123", "aReplyTo");
        StepVerifier.create(messageService.process(ctx))
                    .expectError(IllegalStateException.class)
                    .verify();

        final ArgumentCaptor<String> toCaptor = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<String> textCaptor = ArgumentCaptor.forClass(String.class);

        verify(platformService, never()).replyMessage(toCaptor.capture(), textCaptor.capture());
    }

    @Test
    void testProcess_whenPlatformServiceThrows() {
        when(platformService.replyMessage(anyString(), anyString())).thenThrow(IllegalStateException.class)
                                                                    .thenReturn(Mono.just(TheVoid.INSTANCE));
        when(platformServiceManager.get(eq(Platform.LINE))).thenReturn(platformService);

        final RequestContext ctx = new RequestContext(Platform.LINE, exchange,
                                                      "abc", "aReplyTo");
        StepVerifier.create(messageService.process(ctx))
                    .expectError(IllegalStateException.class)
                    .verify();

        final ArgumentCaptor<String> toCaptor = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<String> textCaptor = ArgumentCaptor.forClass(String.class);

        verify(platformService, times(1))
                .replyMessage(toCaptor.capture(), textCaptor.capture());
        assertThat(toCaptor.getValue()).isEqualTo("aReplyTo");
        assertThat(textCaptor.getValue()).isEqualTo("abc");
    }
}
