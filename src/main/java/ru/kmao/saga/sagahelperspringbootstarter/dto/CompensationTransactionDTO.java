package ru.kmao.saga.sagahelperspringbootstarter.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompensationTransactionDTO {

    private String sagaId;

    private String transactionId;

//    private Long transactionLogId;

    private String compensationReason;

    private String errorMessage;

    private String compensationPayload;
}
