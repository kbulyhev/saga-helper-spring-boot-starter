package ru.kmao.saga.sagahelperspringbootstarter.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionCoordinatorDTO {
    private String transactionId;

    private String sagaId;

    private String sagaIndex;

    private Long transactionLogId;
}
