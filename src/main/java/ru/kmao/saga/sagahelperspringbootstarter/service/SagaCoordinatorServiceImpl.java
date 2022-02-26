package ru.kmao.saga.sagahelperspringbootstarter.service;

import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import ru.kmao.saga.sagahelperspringbootstarter.api.SagaCoordinatorService;
import ru.kmao.saga.sagahelperspringbootstarter.config.SagaProperties;
import ru.kmao.saga.sagahelperspringbootstarter.constants.LogStatus;
import ru.kmao.saga.sagahelperspringbootstarter.constants.TransactionStatus;
import ru.kmao.saga.sagahelperspringbootstarter.dto.BaseServiceDTO;
import ru.kmao.saga.sagahelperspringbootstarter.dto.CompensationTransactionDTO;
import ru.kmao.saga.sagahelperspringbootstarter.dto.EndTransactionRequestDTO;
import ru.kmao.saga.sagahelperspringbootstarter.dto.StartSagaRequestDTO;
import ru.kmao.saga.sagahelperspringbootstarter.dto.StartTransactionRequestDTO;
import ru.kmao.saga.sagahelperspringbootstarter.dto.TransactionCoordinatorDTO;
import ru.kmao.saga.sagahelperspringbootstarter.exception.SagaCoordinatorException;
import ru.kmao.saga.sagahelperspringbootstarter.exception.SagaException;
import ru.kmao.saga.sagahelperspringbootstarter.feign.SagaCoordinatorFeign;
import ru.kmao.saga.sagahelperspringbootstarter.utils.ObjectMapperUtils;
import ru.kmao.saga.sagahelperspringbootstarter.utils.SagaExceptionUtils;

@Service
@Slf4j
public class SagaCoordinatorServiceImpl implements SagaCoordinatorService {

    private final SagaCoordinatorFeign sagaCoordinatorFeign;

    private final SagaProperties sagaProperties;

    @Autowired
    @Qualifier("sagaHelperObjectMapper")
    private ObjectMapper objectMapper;

    public SagaCoordinatorServiceImpl(SagaCoordinatorFeign sagaCoordinatorFeign, SagaProperties sagaProperties) {
        this.sagaCoordinatorFeign = sagaCoordinatorFeign;
        this.sagaProperties = sagaProperties;
    }

    @Override
    public TransactionCoordinatorDTO startSaga(String currentServicePathKey, SagaProperties.ServicePaths servicePaths) {

        SagaProperties.CurrentService currentService = sagaProperties.getCurrentService();

        String compensationUrl = currentService.getUrl() + servicePaths.getCompensationPath();

        StartSagaRequestDTO startSagaRequestDTO = new StartSagaRequestDTO(currentService.getName(), compensationUrl);

        return sagaCoordinatorFeign.startSaga(startSagaRequestDTO);
    }

    @Override
    public TransactionCoordinatorDTO completeErrorSaga(TransactionCoordinatorDTO transactionResponseDTO) {
        return getCompletedTransactionDTO(transactionResponseDTO, LogStatus.ERROR,
                TransactionStatus.FAILED, null, true);
    }

    @Override
    public TransactionCoordinatorDTO completeTransaction(TransactionCoordinatorDTO transactionResponseDTO, Object compensationPayload) {
        return getCompletedTransactionDTO(transactionResponseDTO, LogStatus.SUCCESS,
                TransactionStatus.COMPLETED, compensationPayload, false);
    }

    @Override
    public void checkSagaCoordinatorHealth() {
        try {
            sagaCoordinatorFeign.checkSagaCoordinatorHealth();

        } catch (Exception e) {
            log.error("Exception while coordinator health check", e);
            throw new SagaCoordinatorException("Exception while coordinator health check", e);
        }
    }

    @Override
    public TransactionCoordinatorDTO startTransaction(BaseServiceDTO baseServiceDTO, String transactionServicePath,
                                                      SagaProperties.ServicePaths currentServicePaths) {
        SagaProperties.CurrentService currentService = sagaProperties.getCurrentService();

        if (currentServicePaths.getRequestPath() == null) {
            throw new SagaException("Property saga.currentServicePath.requestPath should not be blank");
        }

        if (currentServicePaths.getCompensationPath() == null) {
            throw new SagaException("Property saga.currentServicePath.compensationPath should not be blank");
        }

        String requestUrl = currentService.getUrl() + currentServicePaths.getRequestPath();
        String compensationUrl = currentService.getUrl() + currentServicePaths.getCompensationPath();

        StartTransactionRequestDTO startTransactionRequestDTO = new StartTransactionRequestDTO();
        startTransactionRequestDTO.setSagaId(baseServiceDTO.getSagaId());
        startTransactionRequestDTO.setParentTransactionId(baseServiceDTO.getTransactionId());
        startTransactionRequestDTO.setTransactionIndex(String.valueOf(Long.parseLong(baseServiceDTO.getSagaIndex()) + 1));
        startTransactionRequestDTO.setRequestPayload(ObjectMapperUtils.getValueAsString(objectMapper, baseServiceDTO));
        startTransactionRequestDTO.setServiceName(currentService.getName());
        startTransactionRequestDTO.setRequestUrl(requestUrl);
        startTransactionRequestDTO.setCompensationUrl(compensationUrl);
        startTransactionRequestDTO.setLogStatus(LogStatus.SUCCESS.name());

        return sagaCoordinatorFeign.startTransaction(startTransactionRequestDTO);
    }

    @Override
    public void compensateTransaction(TransactionCoordinatorDTO transactionCoordinatorDTO, Throwable exception, Object compensationPayload) {
        log.debug("Compensating transaction with id = {}", transactionCoordinatorDTO.getTransactionId());

        String stackTraceChain = SagaExceptionUtils.getStackTraceChain(exception).toString();
        String exceptionNameChain = String.join(" - ",
                sagaProperties.getCurrentService().getName(), SagaExceptionUtils.getExceptionNameChain(exception));

        String compensationPayloadAsString = ObjectMapperUtils.getValueAsString(objectMapper, compensationPayload);

        CompensationTransactionDTO compensationTransactionDTO = new CompensationTransactionDTO(
                transactionCoordinatorDTO.getSagaId(),
                transactionCoordinatorDTO.getTransactionId(),
                exceptionNameChain, stackTraceChain,
                compensationPayloadAsString
        );

        sagaCoordinatorFeign.asyncCompensateTransaction(compensationTransactionDTO);
    }

    @Override
    public void completeTransactionAndCompleteSaga(TransactionCoordinatorDTO transactionCoordinatorDTO, Object compensationPayload) {
        getCompletedTransactionDTO(transactionCoordinatorDTO, LogStatus.SUCCESS,
                TransactionStatus.COMPLETED, compensationPayload, true);
    }

    @Override
    public void successCallback(TransactionCoordinatorDTO transactionCoordinatorDTO) {
        sagaCoordinatorFeign.successCallback(transactionCoordinatorDTO);
    }

    private TransactionCoordinatorDTO getCompletedTransactionDTO(TransactionCoordinatorDTO transactionCoordinatorDTO,
                                                                 LogStatus logStatus, TransactionStatus transactionStatus,
                                                                 Object compensationPayload, boolean completeTransaction) {

        String compensationPayloadAsString = ObjectMapperUtils.getValueAsString(objectMapper, compensationPayload);

        EndTransactionRequestDTO endTransactionRequestDTO = new EndTransactionRequestDTO(
                transactionCoordinatorDTO.getTransactionId(),
                transactionCoordinatorDTO.getSagaId(), compensationPayloadAsString,
                logStatus.name(),
                transactionStatus.name(), completeTransaction);

        return sagaCoordinatorFeign.completeTransaction(endTransactionRequestDTO);
    }
}
