package ru.kmao.saga.sagahelperspringbootstarter.config.feign;

import feign.Retryer;

public class SagaCoordinatorRetryer extends Retryer.Default {

    public SagaCoordinatorRetryer(long period, long maxPeriod, int maxAttempts) {
        super(period, maxPeriod, maxAttempts);
    }
}
