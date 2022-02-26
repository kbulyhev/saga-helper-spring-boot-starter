package ru.kmao.saga.sagahelperspringbootstarter.api;

import ru.kmao.saga.sagahelperspringbootstarter.builder.SagaParamsModel;

public interface StartSagaService<T, R> {

    R startSaga(T request, SagaParamsModel sagaParamsModel);

}
