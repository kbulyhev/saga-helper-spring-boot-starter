package ru.kmao.saga.sagahelperspringbootstarter.exception;

public class SagaCoordinatorException extends RuntimeException {
    public SagaCoordinatorException(String message) {
        super(message);
    }

    public SagaCoordinatorException(String message, Throwable cause) {
        super(message, cause);
    }
}
