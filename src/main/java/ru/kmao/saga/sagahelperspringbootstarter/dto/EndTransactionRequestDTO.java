package ru.kmao.saga.sagahelperspringbootstarter.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class EndTransactionRequestDTO {
    private String transactionId;

    private String sagaId;

    private String compensationPayload;

    private String logStatus;

    private String transactionStatus;

    private boolean completeSaga;

    public EndTransactionRequestDTO(String transactionId, String sagaId, String compensationPayload, String logStatus, String transactionStatus) {
        this.transactionId = transactionId;
        this.sagaId = sagaId;
        this.compensationPayload = compensationPayload;
        this.logStatus = logStatus;
        this.transactionStatus = transactionStatus;
    }
}
