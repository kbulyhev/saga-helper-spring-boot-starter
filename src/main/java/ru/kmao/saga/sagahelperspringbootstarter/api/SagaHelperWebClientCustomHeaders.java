package ru.kmao.saga.sagahelperspringbootstarter.api;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

@Service
public interface SagaHelperWebClientCustomHeaders {
    void addCustomHeaders(HttpHeaders httpHeaders);
}
