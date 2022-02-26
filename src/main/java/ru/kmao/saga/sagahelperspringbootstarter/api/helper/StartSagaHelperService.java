package ru.kmao.saga.sagahelperspringbootstarter.api.helper;

import org.springframework.stereotype.Service;

@Service
public interface StartSagaHelperService<T, R> extends SagaHelperService<R> {

    R executeTransaction(T request);

}
