package com.github.delegacy.youngbot.server.line;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.delegacy.youngbot.server.util.JacksonUtils;

import com.linecorp.bot.model.objectmapper.ModelObjectMapper;

final class LineJacksonUtils {
    private static final ObjectMapper OM = ModelObjectMapper.createNewObjectMapper();

    static <T> T deserialize(String str, Class<T> clazz) {
        return JacksonUtils.deserialize(OM, str, clazz);
    }

    static <T> T deserialize(byte[] bytes, Class<T> clazz) {
        return JacksonUtils.deserialize(OM, bytes, clazz);
    }

    private LineJacksonUtils() {
        // do nothing
    }
}
