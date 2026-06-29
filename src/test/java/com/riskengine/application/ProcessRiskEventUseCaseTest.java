package com.riskengine.application;

import com.riskengine.domain.enums.EventType;
import com.riskengine.domain.enums.RiskDecision;
import com.riskengine.domain.enums.RiskReason;
import com.riskengine.domain.model.RiskEvent;
import com.riskengine.domain.model.RiskResult;
import com.riskengine.infra.InMemoryRiskEventRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ProcessRiskEventUseCaseTest {

    @Test
    void shouldProcessPixEventWithMediumRisk() {
        InMemoryRiskEventRepository repository = new InMemoryRiskEventRepository();
        ProcessRiskEventUseCase useCase = new ProcessRiskEventUseCase(repository);

        RiskEvent event = RiskEvent.newEvent(
                "cus_123",
                EventType.PIX_CREATED,
                new BigDecimal("1250.90"),
                "dev_abc",
                "mobile",
                Map.of("ipRisk", "MEDIUM")
        );

        RiskResult result = useCase.execute(event);

        assertNotNull(result);
        assertEquals(event.eventId(), result.eventId());
        assertEquals("cus_123", result.customerId());
        assertEquals(EventType.PIX_CREATED, result.eventType());
        assertEquals(55, result.riskScore());
        assertEquals(RiskDecision.ALLOW_WITH_MONITORING, result.decision());

        assertTrue(result.reasons().contains(RiskReason.HIGH_AMOUNT));
        assertTrue(result.reasons().contains(RiskReason.MOBILE_CHANNEL));
        assertTrue(result.reasons().contains(RiskReason.MEDIUM_IP_RISK));

        assertTrue(repository.findById(event.eventId()).isPresent());
    }

    @Test
    void shouldCapRiskScoreAtOneHundredAndBlockVeryRiskyEvent() {
        InMemoryRiskEventRepository repository = new InMemoryRiskEventRepository();
        ProcessRiskEventUseCase useCase = new ProcessRiskEventUseCase(repository);

        RiskEvent event = RiskEvent.newEvent(
                "cus_999",
                EventType.LOGIN_SUSPICIOUS,
                new BigDecimal("8000.00"),
                "dev_suspicious",
                "mobile",
                Map.of("ipRisk", "HIGH")
        );

        RiskResult result = useCase.execute(event);

        assertEquals(100, result.riskScore());
        assertEquals(RiskDecision.BLOCK, result.decision());

        assertTrue(result.reasons().contains(RiskReason.VERY_HIGH_AMOUNT));
        assertTrue(result.reasons().contains(RiskReason.SUSPICIOUS_LOGIN));
        assertTrue(result.reasons().contains(RiskReason.MOBILE_CHANNEL));
        assertTrue(result.reasons().contains(RiskReason.HIGH_IP_RISK));
    }

    @Test
    void shouldAllowLowRiskEvent() {
        InMemoryRiskEventRepository repository = new InMemoryRiskEventRepository();
        ProcessRiskEventUseCase useCase = new ProcessRiskEventUseCase(repository);

        RiskEvent event = RiskEvent.newEvent(
                "cus_low",
                EventType.PIX_APPROVED,
                new BigDecimal("100.00"),
                "dev_normal",
                "web",
                Map.of("ipRisk", "LOW")
        );

        RiskResult result = useCase.execute(event);

        assertEquals(0, result.riskScore());
        assertEquals(RiskDecision.ALLOW, result.decision());
        assertTrue(result.reasons().isEmpty());
    }
}