package ru.kmao.saga.sagahelperspringbootstarter.api.helper;

import org.springframework.stereotype.Service;

import ru.kmao.saga.sagahelperspringbootstarter.dto.BaseServiceDTO;

@Service
public interface SagaCompletingHelperService<T, R> extends SagaHelperService<R> {

    R executeTransaction(BaseServiceDTO request, T requestTransaction);

    default BaseServiceDTO getRequestPayloadToNextService(R transactionResult) {
        return null;
    }
}
