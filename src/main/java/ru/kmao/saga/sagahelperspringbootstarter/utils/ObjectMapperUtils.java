package ru.kmao.saga.sagahelperspringbootstarter.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class ObjectMapperUtils {
    public static <T> String getValueAsString(ObjectMapper objectMapper, T value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {

            //fixme
            throw new RuntimeException(e);
        }
    }
}
