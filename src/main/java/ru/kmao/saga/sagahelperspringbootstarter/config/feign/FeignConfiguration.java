package ru.kmao.saga.sagahelperspringbootstarter.config.feign;

import feign.Feign;
import feign.Logger;
import feign.Request;
import feign.Retryer;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.okhttp.OkHttpClient;
import feign.slf4j.Slf4jLogger;
import io.netty.channel.ChannelOption;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import reactor.netty.http.client.HttpClient;
import reactor.netty.transport.logging.AdvancedByteBufFormat;

import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.reactive.function.client.WebClient;

import ru.kmao.saga.sagahelperspringbootstarter.config.SagaProperties;
import ru.kmao.saga.sagahelperspringbootstarter.feign.SagaCoordinatorFeign;

@Configuration
@ConditionalOnProperty(prefix = "saga.coordinator", value = {"url", "connect-timeout-millis", "read-timeout-millis"})
@EnableAsync
public class FeignConfiguration {

    @Autowired
    private SagaProperties sagaProperties;

    @Bean
    @Order(value = 1)
    public SagaCoordinatorFeign buildSagaCoordinatorFeign() {
        SagaProperties.Coordinator coordinator = sagaProperties.getCoordinator();

        return Feign.builder()
                .client(new OkHttpClient())
                .encoder(new JacksonEncoder(createObjectMapper()))
                .decoder(new JacksonDecoder(createObjectMapper()))
                .options(new Request.Options(
                        coordinator.getConnectTimeoutMillis(),
                        TimeUnit.MILLISECONDS,
                        coordinator.getReadTimeoutMillis(),
                        TimeUnit.MILLISECONDS,
                        true))
                .logger(new Slf4jLogger(FeignConfiguration.class.getName()))
                .logLevel(Logger.Level.FULL)
                .retryer(Retryer.NEVER_RETRY)
                .target(SagaCoordinatorFeign.class, coordinator.getUrl());
    }

    @Bean("sagaWebClient")
    public WebClient webClient() {
        final var tcpClient = HttpClient
                .create()
                .wiretap(this.getClass().getCanonicalName(), LogLevel.DEBUG, AdvancedByteBufFormat.TEXTUAL)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, sagaProperties.getConnectTimeoutMillis())
                .doOnConnected(connection -> {
                    connection.addHandlerLast(new ReadTimeoutHandler(sagaProperties.getReadTimeoutMillis(), TimeUnit.MILLISECONDS));
                    connection.addHandlerLast(new WriteTimeoutHandler(sagaProperties.getReadTimeoutMillis(), TimeUnit.MILLISECONDS));
                });

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(tcpClient))
                .build();
    }

    private static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.findAndRegisterModules();
        return mapper;
    }
}
