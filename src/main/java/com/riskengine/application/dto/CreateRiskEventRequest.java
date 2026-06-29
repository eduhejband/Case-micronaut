package com.riskengine.application.dto;

import com.riskengine.domain.enums.EventType;
import io.micronaut.serde.annotation.Serdeable;

import java.math.BigDecimal;
import java.util.Map;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Serdeable
public record CreateRiskEventRequest(
        @NotBlank
        String customerId,

        @NotNull
        EventType eventType,

        @NotNull
        @DecimalMin(value = "0.0")
        BigDecimal amount,

        @NotBlank
        String deviceId,

        @NotBlank
        String channel,

        Map<String, String> metadata
) {
}