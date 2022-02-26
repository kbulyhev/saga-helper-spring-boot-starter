package ru.kmao.saga.sagahelperspringbootstarter.builder;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class SagaParamsModel {
    private String currentServicePathKey;

    private String sagaHelperServiceCode;

    private String targetServiceKey;

    private String targetServicePathKey;

}
