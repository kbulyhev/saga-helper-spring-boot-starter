package ru.kmao.saga.sagahelperspringbootstarter.service.webclient;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import ru.kmao.saga.sagahelperspringbootstarter.config.SagaProperties;
import ru.kmao.saga.sagahelperspringbootstarter.dto.BaseServiceDTO;
import ru.kmao.saga.sagahelperspringbootstarter.dto.TransactionCoordinatorDTO;

@Service
@Slf4j
public abstract class AbstractSagaHelperWebClientServiceImpl {

    public Mono<String> callNextService(BaseServiceDTO baseServiceDTO, TransactionCoordinatorDTO transactionCoordinatorDTO,
                                        SagaProperties.ServicePaths targetServicePath,
                                        SagaProperties.TargetServices targetService) {

        enrichBaseServiceDTO(baseServiceDTO, transactionCoordinatorDTO);

        return exchange(targetService, baseServiceDTO, targetServicePath);
    }

    protected abstract Mono<String> exchange(SagaProperties.TargetServices targetService, BaseServiceDTO baseServiceDTO, SagaProperties.ServicePaths targetServicePath);

    protected abstract HttpHeaders getHeaders();

    private void enrichBaseServiceDTO(BaseServiceDTO baseServiceDTO, TransactionCoordinatorDTO transactionCoordinatorDTO) {
        baseServiceDTO.setSagaId(transactionCoordinatorDTO.getSagaId());
        baseServiceDTO.setTransactionId(transactionCoordinatorDTO.getTransactionId());
        baseServiceDTO.setSagaIndex(transactionCoordinatorDTO.getSagaIndex());
    }
}
