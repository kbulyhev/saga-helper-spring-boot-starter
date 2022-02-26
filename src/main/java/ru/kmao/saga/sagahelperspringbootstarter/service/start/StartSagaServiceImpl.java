package ru.kmao.saga.sagahelperspringbootstarter.service.start;

import lombok.extern.slf4j.Slf4j;

import java.util.List;

import org.springframework.stereotype.Service;

import ru.kmao.saga.sagahelperspringbootstarter.api.SagaCoordinatorService;
import ru.kmao.saga.sagahelperspringbootstarter.api.SagaHelperWebClientService;
import ru.kmao.saga.sagahelperspringbootstarter.api.StartSagaService;
import ru.kmao.saga.sagahelperspringbootstarter.api.helper.SagaHelperService;
import ru.kmao.saga.sagahelperspringbootstarter.api.helper.StartSagaHelperService;
import ru.kmao.saga.sagahelperspringbootstarter.builder.SagaParamsModel;
import ru.kmao.saga.sagahelperspringbootstarter.config.SagaProperties;
import ru.kmao.saga.sagahelperspringbootstarter.dto.BaseServiceDTO;
import ru.kmao.saga.sagahelperspringbootstarter.dto.TransactionCoordinatorDTO;
import ru.kmao.saga.sagahelperspringbootstarter.exception.SagaException;
import ru.kmao.saga.sagahelperspringbootstarter.service.AbstractSagaService;

@Service
@Slf4j
public class StartSagaServiceImpl<T, R> extends AbstractSagaService<T, R> implements StartSagaService<T, R> {

    private final SagaCoordinatorService sagaCoordinatorService;

    public StartSagaServiceImpl(SagaCoordinatorService sagaCoordinatorService,
                                SagaHelperWebClientService sagaHelperWebClientService,
                                SagaProperties sagaProperties,
                                List<SagaHelperService<R>> sagaHelperServiceList) {
        super(sagaProperties, sagaHelperServiceList, sagaCoordinatorService, sagaHelperWebClientService);

//        if (sagaHelperServiceList == null) {
//            sagaHelperServiceList = new ArrayList<>();
//        }
//        this.sagaHelperService = sagaHelperServiceList.stream().collect(Collectors.toMap(StartSagaHelperService::getCode, Function.identity()));
        this.sagaCoordinatorService = sagaCoordinatorService;
//        this.sagaHelperWebClientService = sagaHelperWebClientService;
//        this.sagaProperties = sagaProperties;
    }

    @Override
    public R startSaga(T request, SagaParamsModel sagaParamsModel) {

        log.debug("Starting saga transaction...");

        return executeSaga(null, sagaParamsModel, request);
    }

    @Override
    protected R executeCurrentTransaction(BaseServiceDTO baseServiceDTO, SagaHelperService<R> sagaHelperService,
                                          TransactionCoordinatorDTO transactionCoordinatorDTO, T requestTransaction) {
        //execute transaction
        R transactionResult;
        try {
            transactionResult = ((StartSagaHelperService<T, R>) sagaHelperService).executeTransaction(requestTransaction);

        } catch (Exception e) {
            //if exception is thrown, complete current transaction and complete saga
            sagaCoordinatorService.completeErrorSaga(transactionCoordinatorDTO);
            throw new SagaException(e);
        }

        return transactionResult;
    }

    @Override
    public TransactionCoordinatorDTO startTransaction(String currentServicePathKey, SagaProperties.ServicePaths currentsServicePaths,
                                                      BaseServiceDTO baseServiceDTO, T requestTransaction) {
        return sagaCoordinatorService.startSaga(currentServicePathKey, currentsServicePaths);
    }
}
