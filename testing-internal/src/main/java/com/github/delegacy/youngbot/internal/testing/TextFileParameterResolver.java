package com.github.delegacy.youngbot.internal.testing;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

public class TextFileParameterResolver implements ParameterResolver {
    @SuppressWarnings("ThrowsRuntimeException")
    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException {
        if (parameterContext.getTarget().isEmpty()) {
            return false;
        }

        final var parameter = parameterContext.getParameter();
        if (parameter.getType() != String.class) {
            return false;
        }

        return parameterContext.findAnnotation(TextFile.class)
                               .map(ann -> !ann.value().isEmpty())
                               .orElse(false);
    }

    @SuppressWarnings("ThrowsRuntimeException")
    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException {
        // Checked in supportsParameter
        @SuppressWarnings("OptionalGetWithoutIsPresent")
        final var path = parameterContext.findAnnotation(TextFile.class).map(TextFile::value).get();

        try {
            // Checked in supportsParameter
            @SuppressWarnings("OptionalGetWithoutIsPresent")
            final var text = Files.readString(
                    Path.of(parameterContext.getTarget().get().getClass().getResource(path).toURI()));
            return text;
        } catch (IOException | URISyntaxException e) {
            throw new ParameterResolutionException("Failed to read '" + path + '\'', e);
        }
    }
}
