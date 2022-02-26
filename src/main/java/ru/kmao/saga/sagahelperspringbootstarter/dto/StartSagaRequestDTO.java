package ru.kmao.saga.sagahelperspringbootstarter.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class StartSagaRequestDTO {
    private String serviceName;

    private String compensationUrl;
}
