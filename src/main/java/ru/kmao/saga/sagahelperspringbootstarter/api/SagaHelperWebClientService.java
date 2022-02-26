package ru.kmao.saga.sagahelperspringbootstarter.api;

import reactor.core.publisher.Mono;

import com.fasterxml.jackson.core.JsonProcessingException;

import ru.kmao.saga.sagahelperspringbootstarter.config.SagaProperties;
import ru.kmao.saga.sagahelperspringbootstarter.dto.BaseServiceDTO;
import ru.kmao.saga.sagahelperspringbootstarter.dto.TransactionCoordinatorDTO;

public interface SagaHelperWebClientService {
    Mono<String> callNextService(BaseServiceDTO baseServiceDTO, TransactionCoordinatorDTO transactionCoordinatorDTO,
                                 SagaProperties.ServicePaths targetServicePath, SagaProperties.TargetServices targetService) throws JsonProcessingException;
}
