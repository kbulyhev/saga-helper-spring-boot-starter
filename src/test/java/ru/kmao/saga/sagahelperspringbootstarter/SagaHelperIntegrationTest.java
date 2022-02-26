package ru.kmao.saga.sagahelperspringbootstarter;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import ru.kmao.saga.sagahelperspringbootstarter.api.SagaCoordinatorService;
import ru.kmao.saga.sagahelperspringbootstarter.api.StartSagaService;
import ru.kmao.saga.sagahelperspringbootstarter.config.SagaProperties;

@SpringBootTest
@ActiveProfiles("test")
@ContextConfiguration(classes = TestApplication.class)
class SagaHelperIntegrationTest {

    @Autowired
    private SagaProperties sagaProperties;

    @Autowired
    private StartSagaService<String, String> startSagaService;

    @Autowired
    private SagaCoordinatorService sagaCoordinatorService;

    @Test
    void contextLoads() {
        System.out.println();
    }

}
