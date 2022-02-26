package ru.kmao.saga.sagahelperspringbootstarter.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("saga")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SagaProperties {
    private static final Integer DEFAULT_CONNECT_TIMEOUT_MILLS = 10000;
    private static final Long DEFAULT_READ_TIMEOUT_MILLS = 60000L;

    private Integer connectTimeoutMillis = DEFAULT_CONNECT_TIMEOUT_MILLS;

    private Long readTimeoutMillis = DEFAULT_READ_TIMEOUT_MILLS;

    private Coordinator coordinator = new Coordinator();

    private CurrentService currentService;

    private Map<String, TargetServices> targetServices;

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    public static class Coordinator {
        private String url;

        private Integer connectTimeoutMillis = DEFAULT_CONNECT_TIMEOUT_MILLS;

        private Long readTimeoutMillis = DEFAULT_READ_TIMEOUT_MILLS;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TargetServices {
        private String url;

        private Map<String, ServicePaths> paths;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ServicePaths {
        private String requestPath;

        private String compensationPath;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CurrentService {
        private String name;

        private String url;

        private Map<String, ServicePaths> paths;
    }
}
