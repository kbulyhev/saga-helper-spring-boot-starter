package ru.kmao.saga.sagahelperspringbootstarter.service;

import lombok.extern.slf4j.Slf4j;

import java.net.ConnectException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import ru.kmao.saga.sagahelperspringbootstarter.api.SagaCoordinatorService;
import ru.kmao.saga.sagahelperspringbootstarter.api.SagaHelperWebClientService;
import ru.kmao.saga.sagahelperspringbootstarter.api.helper.SagaHelperService;
import ru.kmao.saga.sagahelperspringbootstarter.builder.SagaParamsModel;
import ru.kmao.saga.sagahelperspringbootstarter.config.SagaProperties;
import ru.kmao.saga.sagahelperspringbootstarter.dto.BaseServiceDTO;
import ru.kmao.saga.sagahelperspringbootstarter.dto.TransactionCoordinatorDTO;
import ru.kmao.saga.sagahelperspringbootstarter.exception.SagaException;
import ru.kmao.saga.sagahelperspringbootstarter.utils.SagaUtils;

@Service
@Slf4j
public abstract class AbstractSagaService<T, R> {

    private final Map<String, SagaHelperService<R>> sagaHelperServices;

    private final SagaProperties sagaProperties;

    private final SagaCoordinatorService sagaCoordinatorService;

    private final SagaHelperWebClientService webClientService;

    public AbstractSagaService(SagaProperties sagaProperties, List<SagaHelperService<R>> sagaHelperServiceList,
                               SagaCoordinatorService sagaCoordinatorService,
                               SagaHelperWebClientService webClientService) {
        this.sagaProperties = sagaProperties;
        this.sagaHelperServices = sagaHelperServiceList.stream().collect(Collectors.toMap(SagaHelperService::getCode, Function.identity()));
        this.sagaCoordinatorService = sagaCoordinatorService;
        this.webClientService = webClientService;
    }

    public R executeSaga(BaseServiceDTO requestServiceDTO, SagaParamsModel sagaParamsModel, T requestTransaction) {

        sagaCoordinatorService.checkSagaCoordinatorHealth();

        checkProperties(sagaProperties);

        String currentServicePathKey = sagaParamsModel.getCurrentServicePathKey();
        String sagaHelperServiceCode = sagaParamsModel.getSagaHelperServiceCode();
        String serviceKey = sagaParamsModel.getTargetServiceKey();
        String targetServicePathKey = sagaParamsModel.getTargetServicePathKey();

        Map<String, SagaProperties.ServicePaths> currentPaths = sagaProperties.getCurrentService().getPaths();
        Map<String, SagaProperties.TargetServices> targetServices = sagaProperties.getTargetServices();

        SagaProperties.ServicePaths currentsServicePaths = SagaUtils.getMapValueByKeyOrThrowsException(currentServicePathKey, currentPaths,
                "Unable to find configuration from yaml/properties file by code " + currentServicePathKey);
        SagaHelperService<R> sagaHelperService = SagaUtils.getMapValueByKeyOrThrowsException(sagaHelperServiceCode,
                this.sagaHelperServices, "Unable to find saga helper service by code " + currentServicePathKey);

        SagaProperties.TargetServices targetService = getTargetServices(serviceKey, targetServices);
        SagaProperties.ServicePaths targetServicePath = getTargetServicePaths(targetServicePathKey, targetService);

        TransactionCoordinatorDTO transactionCoordinatorDTO = startTransaction(currentServicePathKey, currentsServicePaths, requestServiceDTO, requestTransaction);

        //get result from transaction
        R transactionResult = executeCurrentTransaction(requestServiceDTO, sagaHelperService,
                transactionCoordinatorDTO, requestTransaction);

        //get compensation payload by transaction result
        Object compensationPayload = null;
        try {
            compensationPayload = sagaHelperService.getCompensationPayload(transactionResult);
        } catch (Exception e) {
            compensateTransaction(transactionCoordinatorDTO, e, "Error while get compensation payload");
        }

        //call coordinator api to complete transaction
        completeTransaction(transactionCoordinatorDTO, compensationPayload);

        //chain responsibility to next service
        callNextService(transactionCoordinatorDTO, transactionResult, targetService, targetServicePath, sagaHelperService);

//        if saga is completed send success collback
//        sagaCoordinatorService.successCallback(transactionCoordinatorDTO);

        return transactionResult;
    }

    private void checkProperties(SagaProperties sagaProperties) {
        if (sagaProperties.getCoordinator().getUrl() == null) {
            throw new SagaException("Saga configuration exception: Can't find property for configuration saga coordinator saga.coordinator.url");
        }

//        SagaProperties.CurrentService currentService = sagaProperties.getCurrentService();
//
//        if (currentService == null) {
//            throw new SagaException("Saga configuration exception: Can't find property for configuration sag saga.currentService.name");
//        }
    }

    protected SagaProperties.ServicePaths getTargetServicePaths(String targetServicePathKey, SagaProperties.TargetServices targetService) {
        return SagaUtils.getMapValueByKeyOrThrowsException(targetServicePathKey,
                targetService.getPaths(), "Incorrect key for configuration target services paths");
    }

    protected SagaProperties.TargetServices getTargetServices(String serviceKey, Map<String, SagaProperties.TargetServices> targetServices) {
        return SagaUtils.getMapValueByKeyOrThrowsException(serviceKey,
                targetServices, "Incorrect key for configuration target services");
    }

    protected abstract R executeCurrentTransaction(BaseServiceDTO nextServiceDTO, SagaHelperService<R> sagaHelperService,
                                                   TransactionCoordinatorDTO transactionCoordinatorDTO,
                                                   T requestTransaction);

    protected void callNextService(TransactionCoordinatorDTO transactionCoordinatorDTO,
                                   R transactionResult, SagaProperties.TargetServices targetService,
                                   SagaProperties.ServicePaths targetServicePath, SagaHelperService<R> sagaHelperService) {

        String nextServiceUrl = targetService.getUrl() + targetServicePath.getRequestPath();
        try {

            BaseServiceDTO baseServiceDTO = sagaHelperService.getRequestPayloadToNextService(transactionResult);
            webClientService.callNextService(baseServiceDTO, transactionCoordinatorDTO, targetServicePath, targetService)
                    .doOnError(error -> {
                                //handle only connection exception
                                if (error.getCause() != null && error.getCause() instanceof ConnectException) {
                                    compensateTransaction(transactionCoordinatorDTO, error, String.format("Failed to call next service %s ", nextServiceUrl));
                                }

                                log.error("Exception while call next service in transaction", error);
                            }
                    )
                    .subscribe();
        } catch (Exception e) {
            compensateTransaction(transactionCoordinatorDTO, e, String.format("Failed to call next service %s ", nextServiceUrl));
        }
    }

    protected void completeTransaction(TransactionCoordinatorDTO transactionCoordinatorDTO, Object compensationPayload) {
        try {
            // if transaction is ok, complete current transaction and start other transaction along the chain
            sagaCoordinatorService.completeTransaction(transactionCoordinatorDTO, compensationPayload);

        } catch (Exception e) {
            compensateTransactionWithCompensationPayload(transactionCoordinatorDTO, e,
                    "Failed to complete transaction by coordinator",
                    compensationPayload);
        }
    }

    protected void compensateTransactionWithCompensationPayload(TransactionCoordinatorDTO transactionCoordinatorDTO, Exception e,
                                                                String errorMessage, Object compensationPayload) {
        log.debug("Try to compensate current transaction (transactionId = {})", transactionCoordinatorDTO.getTransactionId());

        // if something go wrong compensate current transaction
        sagaCoordinatorService.compensateTransaction(transactionCoordinatorDTO, e, compensationPayload);
        throw new SagaException(errorMessage, e);
    }

    protected void compensateTransaction(TransactionCoordinatorDTO transactionCoordinatorDTO,
                                         Throwable e, String errorMessage) {
        log.debug("Try to compensate current transaction (transactionId = {})", transactionCoordinatorDTO.getTransactionId());

        // if something go wrong compensate current transaction
        sagaCoordinatorService.compensateTransaction(transactionCoordinatorDTO, e, null);
        throw new SagaException(errorMessage, e);
    }

    public abstract TransactionCoordinatorDTO startTransaction(String currentServicePathKey,
                                                               SagaProperties.ServicePaths currentsServicePaths,
                                                               BaseServiceDTO baseServiceDTO, T requestTransaction);
}
