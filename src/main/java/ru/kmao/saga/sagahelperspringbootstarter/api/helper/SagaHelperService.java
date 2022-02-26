package ru.kmao.saga.sagahelperspringbootstarter.api.helper;

import ru.kmao.saga.sagahelperspringbootstarter.dto.BaseServiceDTO;

public interface SagaHelperService<R> {

    Object getCompensationPayload(R transactionResult);

    BaseServiceDTO getRequestPayloadToNextService(R transactionResult);

    String getCode();
}