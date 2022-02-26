package ru.kmao.saga.sagahelperspringbootstarter.utils;

import java.util.Map;

public final class SagaUtils {

    public static <T> T getMapValueByKeyOrThrowsException(String key, Map<String, T> map, String errorMessage) {
        if (map == null) {
            throw new RuntimeException(errorMessage);
        }

        if (!map.containsKey(key)) {
            throw new RuntimeException(errorMessage);
        }

        return map.get(key);
    }
}
