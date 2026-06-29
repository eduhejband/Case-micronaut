package com.riskengine.infra;

import com.riskengine.domain.enums.EventType;
import com.riskengine.domain.enums.RiskDecision;
import com.riskengine.domain.enums.RiskReason;
import com.riskengine.domain.model.RiskEvent;
import com.riskengine.domain.model.RiskResult;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryRiskEventRepositoryTest {

    @Test
    void shouldSaveAndFindRiskEventById() {
        InMemoryRiskEventRepository repository = new InMemoryRiskEventRepository();

        RiskEvent event = createEvent("cus_123");
        RiskResult result = createResult(event, 55, RiskDecision.ALLOW_WITH_MONITORING);

        repository.save(event, result);

        Optional<RiskResult> found = repository.findById(event.eventId());

        assertTrue(found.isPresent());
        assertEquals(event.eventId(), found.get().eventId());
        assertEquals("cus_123", found.get().customerId());
    }

    @Test
    void shouldReturnEmptyWhenEventDoesNotExist() {
        InMemoryRiskEventRepository repository = new InMemoryRiskEventRepository();

        Optional<RiskResult> found = repository.findById("missing-id");

        assertTrue(found.isEmpty());
    }

    @Test
    void shouldFindRiskEventsByCustomerId() {
        InMemoryRiskEventRepository repository = new InMemoryRiskEventRepository();

        RiskEvent firstEvent = createEvent("cus_123");
        RiskEvent secondEvent = createEvent("cus_123");
        RiskEvent anotherCustomerEvent = createEvent("cus_999");

        repository.save(firstEvent, createResult(firstEvent, 10, RiskDecision.ALLOW));
        repository.save(secondEvent, createResult(secondEvent, 60, RiskDecision.REVIEW));
        repository.save(anotherCustomerEvent, createResult(anotherCustomerEvent, 100, RiskDecision.BLOCK));

        List<RiskResult> results = repository.findByCustomerId("cus_123");

        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(result -> result.customerId().equals("cus_123")));
    }

    private RiskEvent createEvent(String customerId) {
        return RiskEvent.newEvent(
                customerId,
                EventType.PIX_CREATED,
                new BigDecimal("1250.90"),
                "dev_abc",
                "mobile",
                Map.of("ipRisk", "MEDIUM")
        );
    }

    private RiskResult createResult(RiskEvent event, int score, RiskDecision decision) {
        return new RiskResult(
                event.eventId(),
                event.customerId(),
                event.eventType(),
                score,
                decision,
                List.of(RiskReason.HIGH_AMOUNT),
                Instant.now()
        );
    }
}