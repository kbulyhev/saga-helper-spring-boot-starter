package ru.kmao.saga.sagahelperspringbootstarter.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StartTransactionRequestDTO {

    private String sagaId;

    private String parentTransactionId;

    private String serviceName;

    private String transactionIndex;

    private String requestPayload;

    private String compensationPayload;

    private String requestUrl;

    private String compensationUrl;

    private String logStatus;
}
