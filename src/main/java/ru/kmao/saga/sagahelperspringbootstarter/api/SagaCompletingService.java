package ru.kmao.saga.sagahelperspringbootstarter.api;

import ru.kmao.saga.sagahelperspringbootstarter.builder.SagaParamsModel;
import ru.kmao.saga.sagahelperspringbootstarter.dto.BaseServiceDTO;

public interface SagaCompletingService<T, R> {
    R completeSaga(BaseServiceDTO baseServiceDTO, SagaParamsModel sagaParamsModel, T requestTransactionData);
}
