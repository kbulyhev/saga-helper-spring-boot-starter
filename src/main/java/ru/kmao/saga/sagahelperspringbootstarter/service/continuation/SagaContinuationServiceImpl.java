package ru.kmao.saga.sagahelperspringbootstarter.service.continuation;

import lombok.extern.slf4j.Slf4j;

import java.util.List;

import org.springframework.stereotype.Service;

import ru.kmao.saga.sagahelperspringbootstarter.api.SagaContinuationService;
import ru.kmao.saga.sagahelperspringbootstarter.api.SagaCoordinatorService;
import ru.kmao.saga.sagahelperspringbootstarter.api.SagaHelperWebClientService;
import ru.kmao.saga.sagahelperspringbootstarter.api.helper.SagaContinuationHelperService;
import ru.kmao.saga.sagahelperspringbootstarter.api.helper.SagaHelperService;
import ru.kmao.saga.sagahelperspringbootstarter.builder.SagaParamsModel;
import ru.kmao.saga.sagahelperspringbootstarter.config.SagaProperties;
import ru.kmao.saga.sagahelperspringbootstarter.dto.BaseServiceDTO;
import ru.kmao.saga.sagahelperspringbootstarter.dto.TransactionCoordinatorDTO;
import ru.kmao.saga.sagahelperspringbootstarter.service.AbstractSagaService;

@Service
@Slf4j
public class SagaContinuationServiceImpl<T, R> extends AbstractSagaService<T, R> implements SagaContinuationService<T, R> {

    private final SagaCoordinatorService sagaCoordinatorService;

    public SagaContinuationServiceImpl(List<SagaHelperService<R>> sagaContinuationHelperServiceList,
                                       SagaCoordinatorService sagaCoordinatorService,
                                       SagaProperties sagaProperties, SagaHelperWebClientService sagaHelperWebClientService) {
        super(sagaProperties, sagaContinuationHelperServiceList, sagaCoordinatorService, sagaHelperWebClientService);
        this.sagaCoordinatorService = sagaCoordinatorService;
    }

    @Override
    public R continueTransaction(BaseServiceDTO baseServiceDTO, SagaParamsModel sagaParamsModel, T requestTransactionData) {

        log.debug("Continue saga transaction with transaction_id = {}", baseServiceDTO.getTransactionId());

        return executeSaga(baseServiceDTO, sagaParamsModel, requestTransactionData);
    }

    @Override
    protected R executeCurrentTransaction(BaseServiceDTO nextServiceDTO, SagaHelperService<R> sagaHelperService,
                                          TransactionCoordinatorDTO transactionCoordinatorDTO, T requestTransaction) {
        R transactionResult = null;
        try {
            transactionResult = ((SagaContinuationHelperService<T, R>) sagaHelperService).executeTransaction(nextServiceDTO, requestTransaction);
        } catch (Exception e) {
            //if exception is thrown, complete current transaction and complete saga
            compensateTransaction(transactionCoordinatorDTO, e, "Error while executing current transaction...");
        }

        return transactionResult;
    }

    @Override
    public TransactionCoordinatorDTO startTransaction(String currentServicePathKey, SagaProperties.ServicePaths currentsServicePaths,
                                                      BaseServiceDTO baseServiceDTO, T requestTransaction) {
        try {
            return sagaCoordinatorService.startTransaction(baseServiceDTO, currentServicePathKey, currentsServicePaths);
        } catch (Exception e) {

            //if exception is thrown, complete current transaction and complete saga
            compensateTransaction(new TransactionCoordinatorDTO(baseServiceDTO.getTransactionId(),
                            baseServiceDTO.getSagaId(), baseServiceDTO.getSagaIndex(), null),
                    e, "Error while starting saga");
        }

        return null;
    }
}
