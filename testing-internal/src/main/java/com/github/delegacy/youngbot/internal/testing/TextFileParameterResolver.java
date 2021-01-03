package com.github.delegacy.youngbot.internal.testing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

@SuppressWarnings("ThrowsRuntimeException")
public class TextFileParameterResolver implements ParameterResolver {
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

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException {
        // Checked by supportsParameter
        //noinspection OptionalGetWithoutIsPresent
        final var path = parameterContext.findAnnotation(TextFile.class).map(TextFile::value).get();

        // Checked by supportsParameter
        //noinspection OptionalGetWithoutIsPresent
        try (InputStream stream =
                     parameterContext.getTarget()
                                     .get()
                                     .getClass()
                                     .getResourceAsStream(path)) {
            if (stream == null) {
                throw new ParameterResolutionException("No resource is found for '" + path + '\'');
            }

            final var sb = new StringBuilder();
            try (Reader reader = new BufferedReader(new InputStreamReader(stream))) {
                int c;
                while ((c = reader.read()) != -1) {
                    sb.append((char) c);
                }
            }
            return sb.toString();
        } catch (IOException e) {
            throw new ParameterResolutionException("Failed to read '" + path + '\'', e);
        }
    }
}
