package ru.kmao.saga.sagahelperspringbootstarter.service.complete;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import ru.kmao.saga.sagahelperspringbootstarter.api.SagaCompletingService;
import ru.kmao.saga.sagahelperspringbootstarter.api.SagaCoordinatorService;
import ru.kmao.saga.sagahelperspringbootstarter.api.SagaHelperWebClientService;
import ru.kmao.saga.sagahelperspringbootstarter.api.helper.SagaCompletingHelperService;
import ru.kmao.saga.sagahelperspringbootstarter.api.helper.SagaHelperService;
import ru.kmao.saga.sagahelperspringbootstarter.builder.SagaParamsModel;
import ru.kmao.saga.sagahelperspringbootstarter.config.SagaProperties;
import ru.kmao.saga.sagahelperspringbootstarter.dto.BaseServiceDTO;
import ru.kmao.saga.sagahelperspringbootstarter.dto.TransactionCoordinatorDTO;
import ru.kmao.saga.sagahelperspringbootstarter.service.AbstractSagaService;

@Service
@Slf4j
public class SagaCompletingServiceImpl<T, R> extends AbstractSagaService<T, R> implements SagaCompletingService<T, R> {

    private final SagaCoordinatorService sagaCoordinatorService;

    public SagaCompletingServiceImpl(List<SagaHelperService<R>> sagaContinuationHelperServiceList,
                                     SagaCoordinatorService sagaCoordinatorService, SagaProperties sagaProperties,
                                     SagaHelperWebClientService sagaHelperWebClientService) {
        super(sagaProperties, sagaContinuationHelperServiceList, sagaCoordinatorService, sagaHelperWebClientService);
        this.sagaCoordinatorService = sagaCoordinatorService;
    }

    @Override
    public R completeSaga(BaseServiceDTO baseServiceDTO, SagaParamsModel sagaParamsModel, T requestTransactionData) {

        log.debug("Complete saga transaction with transaction_id = {}", baseServiceDTO.getTransactionId());

        return executeSaga(baseServiceDTO, sagaParamsModel, requestTransactionData);
    }

    @Override
    protected R executeCurrentTransaction(BaseServiceDTO nextServiceDTO, SagaHelperService<R> sagaHelperService,
                                          TransactionCoordinatorDTO transactionCoordinatorDTO, T requestTransaction) {
        R transactionResult = null;
        try {
            transactionResult = ((SagaCompletingHelperService<T, R>) sagaHelperService).executeTransaction(nextServiceDTO, requestTransaction);
        } catch (Exception e) {
            //if exception is thrown, complete current transaction and complete saga
            compensateTransaction(transactionCoordinatorDTO, e, "Failed to execute current transaction transaction");
        }

        return transactionResult;
    }

    @Override
    public TransactionCoordinatorDTO startTransaction(String currentServicePathKey, SagaProperties.ServicePaths currentsServicePaths,
                                                      BaseServiceDTO baseServiceDTO, T requestTransaction) {
        try {
            return sagaCoordinatorService.startTransaction(baseServiceDTO, currentServicePathKey, currentsServicePaths);
        } catch (Exception e) {

            compensateTransaction(new TransactionCoordinatorDTO(baseServiceDTO.getTransactionId(),
                    baseServiceDTO.getSagaId(), baseServiceDTO.getSagaIndex(), null), e, "Failed to start transaction by coordinator");
        }

        return null;
    }

    @Override
    public void completeTransaction(TransactionCoordinatorDTO transactionCoordinatorDTO, Object compensationPayload) {
        try {
            sagaCoordinatorService.completeTransactionAndCompleteSaga(transactionCoordinatorDTO, compensationPayload);

        } catch (Exception e) {
            compensateTransactionWithCompensationPayload(transactionCoordinatorDTO, e, "Failed to complete transaction by coordinator", compensationPayload);
        }
    }

    @Override
    protected void callNextService(TransactionCoordinatorDTO transactionCoordinatorDTO, R transactionResult, SagaProperties.TargetServices targetService,
                                   SagaProperties.ServicePaths targetServicePath,
                                   SagaHelperService<R> sagaHelperService) {
    }

    @Override
    protected SagaProperties.ServicePaths getTargetServicePaths(String targetServicePathKey, SagaProperties.TargetServices targetService) {
        return null;
    }

    @Override
    protected SagaProperties.TargetServices getTargetServices(String serviceKey, Map<String, SagaProperties.TargetServices> targetServices) {
        return null;
    }
}
