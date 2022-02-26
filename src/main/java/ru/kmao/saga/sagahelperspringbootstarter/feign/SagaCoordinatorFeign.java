package ru.kmao.saga.sagahelperspringbootstarter.feign;

import feign.Headers;
import feign.RequestLine;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import ru.kmao.saga.sagahelperspringbootstarter.dto.CompensationTransactionDTO;
import ru.kmao.saga.sagahelperspringbootstarter.dto.CompleteSagaDTO;
import ru.kmao.saga.sagahelperspringbootstarter.dto.EndTransactionRequestDTO;
import ru.kmao.saga.sagahelperspringbootstarter.dto.StartSagaRequestDTO;
import ru.kmao.saga.sagahelperspringbootstarter.dto.StartTransactionRequestDTO;
import ru.kmao.saga.sagahelperspringbootstarter.dto.TransactionCoordinatorDTO;

@Service
public interface SagaCoordinatorFeign {

    @RequestLine("GET actuator/health")
    @Headers("Content-Type: application/json")
    void checkSagaCoordinatorHealth();

    @RequestLine("POST /saga/start")
    @Headers("Content-Type: application/json")
    TransactionCoordinatorDTO startSaga(@RequestBody StartSagaRequestDTO startSagaRequestDTO);

    @RequestLine("POST /saga/complete")
    @Headers("Content-Type: application/json")
    void completeSaga(@RequestBody CompleteSagaDTO completeSagaDTO);

    @RequestLine("POST /saga/transaction/complete")
    @Headers("Content-Type: application/json")
    TransactionCoordinatorDTO completeTransaction(@RequestBody EndTransactionRequestDTO completeSagaDTO);

    @RequestLine("POST /saga/transaction/start")
    @Headers("Content-Type: application/json")
    TransactionCoordinatorDTO startTransaction(StartTransactionRequestDTO startTransactionRequestDTO);

    @RequestLine("POST /saga/transaction/compensate")
    @Headers("Content-Type: application/json")
    @Async
    void asyncCompensateTransaction(@RequestBody CompensationTransactionDTO compensationTransactionDTO);

    @RequestLine("POST /saga/success-callback")
    @Headers("Content-Type: application/json")
    void successCallback(@RequestBody TransactionCoordinatorDTO transactionCoordinatorDTO);
}
