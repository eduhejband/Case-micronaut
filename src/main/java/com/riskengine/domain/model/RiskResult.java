package com.riskengine.domain.model;

import com.riskengine.domain.enums.EventType;
import com.riskengine.domain.enums.RiskDecision;
import com.riskengine.domain.enums.RiskReason;
import io.micronaut.serde.annotation.Serdeable;

import java.time.Instant;
import java.util.List;

@Serdeable
public record RiskResult(
        String eventId,
        String customerId,
        EventType eventType,
        int riskScore,
        RiskDecision decision,
        List<RiskReason> reasons,
        Instant processedAt
) {
}