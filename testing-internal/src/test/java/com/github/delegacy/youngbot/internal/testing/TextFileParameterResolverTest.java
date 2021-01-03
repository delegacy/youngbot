package com.github.delegacy.youngbot.internal.testing;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(TextFileParameterResolver.class)
class TextFileParameterResolverTest {
    @Test
    void testResolveParameter(@TextFile("text.txt") String text) throws Exception {
        assertThat(text).isEqualTo("Hello, World!");
    }
}
