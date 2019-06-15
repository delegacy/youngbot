package com.github.delegacy.youngbot.server.message.handler.todo;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.regex.Matcher;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TodoMessageHandlerTest {
    @SuppressWarnings("unused")
    @Mock
    private TodoService todoService;

    @InjectMocks
    private TodoMessageHandler handler;

    @Test
    void testPattern() {
        Matcher matcher = handler.pattern().matcher("/todo");
        assertThat(matcher.matches()).isTrue();
        assertThat(matcher.group("args")).isNull();

        matcher = handler.pattern().matcher("/todo ls");
        assertThat(matcher.matches()).isTrue();
        assertThat(matcher.group("args")).isEqualTo("ls");

        matcher = handler.pattern().matcher("/todo a \"THING I NEED TO DO +project @context\"");
        assertThat(matcher.matches()).isTrue();
        assertThat(matcher.group("args")).isEqualTo("a \"THING I NEED TO DO +project @context\"");
    }
}
