package com.riskengine.domain.model;

import com.riskengine.domain.enums.EventType;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Serdeable
public record RiskEvent(
        String eventId,

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

        Map<String, String> metadata,

        Instant createdAt
) {

    public static RiskEvent newEvent(
            String customerId,
            EventType eventType,
            BigDecimal amount,
            String deviceId,
            String channel,
            Map<String, String> metadata
    ) {
        return new RiskEvent(
                UUID.randomUUID().toString(),
                customerId,
                eventType,
                amount,
                deviceId,
                channel,
                metadata == null ? Map.of() : Map.copyOf(metadata),
                Instant.now()
        );
    }
}