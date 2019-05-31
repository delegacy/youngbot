package com.github.delegacy.youngbot.server.junit;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Parameter;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import com.google.common.io.CharStreams;
import com.google.common.io.Closeables;

@SuppressWarnings("ThrowsRuntimeException")
public class TextFileParameterResolver implements ParameterResolver {
    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException {

        if (parameterContext.getTarget().isEmpty()) {
            return false;
        }

        final Parameter parameter = parameterContext.getParameter();
        if (parameter.getType() != String.class) {
            return false;
        }

        final TextFile annotation = parameterContext.getParameter().getAnnotation(TextFile.class);
        if (annotation == null) {
            return false;
        }

        return !annotation.value().isEmpty();
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException {

        final String path = parameterContext.getParameter().getAnnotation(TextFile.class).value();

        @SuppressWarnings("OptionalGetWithoutIsPresent")
        final InputStream stream = parameterContext.getTarget().get().getClass().getResourceAsStream(path);
        try {
            if (stream == null) {
                throw new ParameterResolutionException('\'' + path + "' not found");
            }

            //noinspection UnstableApiUsage
            return CharStreams.toString(new InputStreamReader(stream));
        } catch (IOException e) {
            throw new ParameterResolutionException("Failed to read from '" + path + '\'', e);
        } finally {
            //noinspection UnstableApiUsage
            Closeables.closeQuietly(stream);
        }
    }
}
