package ru.kmao.saga.sagahelperspringbootstarter.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.util.CollectionUtils;

public class SagaExceptionUtils {

    public static String getStacktrace(Throwable throwable) {
        if (throwable == null) {
            return null;
        }
        String stackTrace = ExceptionUtils.getStackTrace(throwable);

        if (StringUtils.isNotBlank(stackTrace)) {
            stackTrace = stackTrace.replaceAll("'", StringUtils.EMPTY);
        }
        return stackTrace;
    }

    public static List<String> getStackTraceChain(Throwable throwable) {
        List<String> result = new ArrayList<>();
        Throwable chainedException = throwable;
        while (chainedException != null) {
            String stackTrace = ExceptionUtils.getStackTrace(chainedException);
            if (StringUtils.isNotBlank(stackTrace)) {
                stackTrace = stackTrace.replaceAll("'", StringUtils.EMPTY);
            }
            result.add(stackTrace);
            chainedException = chainedException.getCause();
        }
        return result; //["THIRD EXCEPTION", "SECOND EXCEPTION", "FIRST EXCEPTION"]
    }

    public static String getExceptionNameChain(Throwable throwable) {
        List<String> result = new ArrayList<>();
        Throwable chainedException = throwable;
        while (chainedException != null) {
            String exceptionName = chainedException.getClass().getSimpleName();
            result.add(exceptionName);
            chainedException = chainedException.getCause();
        }

        if (CollectionUtils.isEmpty(result)){
            return StringUtils.EMPTY;
        }
        return String.join(" -> ", result);
    }
}
