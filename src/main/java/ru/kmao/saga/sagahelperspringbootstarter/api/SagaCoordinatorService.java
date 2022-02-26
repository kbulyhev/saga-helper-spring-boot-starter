package ru.kmao.saga.sagahelperspringbootstarter.api;

import ru.kmao.saga.sagahelperspringbootstarter.config.SagaProperties;
import ru.kmao.saga.sagahelperspringbootstarter.dto.BaseServiceDTO;
import ru.kmao.saga.sagahelperspringbootstarter.dto.TransactionCoordinatorDTO;

public interface SagaCoordinatorService {
    TransactionCoordinatorDTO startSaga(String currentServicePathKey, SagaProperties.ServicePaths servicePaths);

    TransactionCoordinatorDTO completeErrorSaga(TransactionCoordinatorDTO transactionResponseDTO);

    TransactionCoordinatorDTO completeTransaction(TransactionCoordinatorDTO transactionResponseDTO, Object compensationPayload);

    void checkSagaCoordinatorHealth();

    TransactionCoordinatorDTO startTransaction(BaseServiceDTO baseServiceDTO, String transactionServicePath, SagaProperties.ServicePaths servicePaths);

    void compensateTransaction(TransactionCoordinatorDTO transactionCoordinatorDTO, Throwable exception, Object compensationPayload);

    void completeTransactionAndCompleteSaga(TransactionCoordinatorDTO transactionCoordinatorDTO, Object compensationPayload);

    void successCallback(TransactionCoordinatorDTO transactionCoordinatorDTO);
}
