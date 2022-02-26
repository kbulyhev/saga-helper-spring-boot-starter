package ru.kmao.saga.sagahelperspringbootstarter.service.webclient;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import ru.kmao.saga.sagahelperspringbootstarter.api.SagaHelperWebClientCustomHeaders;
import ru.kmao.saga.sagahelperspringbootstarter.api.SagaHelperWebClientService;
import ru.kmao.saga.sagahelperspringbootstarter.config.SagaProperties;
import ru.kmao.saga.sagahelperspringbootstarter.dto.BaseServiceDTO;

@Service
@Slf4j
public class SagaHelperWebClientServiceImpl extends AbstractSagaHelperWebClientServiceImpl implements SagaHelperWebClientService {

    private final WebClient webClient;

    private final SagaProperties sagaProperties;

    @Autowired(required = false)
    private SagaHelperWebClientCustomHeaders sagaHelperWebClientCustomHeaders;

    public SagaHelperWebClientServiceImpl(@Qualifier("sagaWebClient") WebClient webClient,
                                          SagaProperties sagaProperties) {
        this.webClient = webClient;
        this.sagaProperties = sagaProperties;
    }

    @Override
    protected Mono<String> exchange(SagaProperties.TargetServices targetService, BaseServiceDTO baseServiceDTO, SagaProperties.ServicePaths targetServicePath) {

        String nextServiceUrl = targetService.getUrl() + targetServicePath.getRequestPath();

        return webClient.post()
                .uri(nextServiceUrl)
                .headers(httpHeaders -> getHeaders())
                .body(BodyInserters.fromValue(baseServiceDTO))
                .retrieve()
                .bodyToMono(String.class);
    }

    @Override
    protected HttpHeaders getHeaders() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        if (sagaHelperWebClientCustomHeaders != null) {
            sagaHelperWebClientCustomHeaders.addCustomHeaders(httpHeaders);
        }

        return httpHeaders;
    }
}
