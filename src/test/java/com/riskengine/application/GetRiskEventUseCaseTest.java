package com.riskengine.application;

import com.riskengine.domain.enums.EventType;
import com.riskengine.domain.enums.RiskDecision;
import com.riskengine.domain.enums.RiskReason;
import com.riskengine.domain.model.RiskEvent;
import com.riskengine.domain.model.RiskResult;
import com.riskengine.infra.InMemoryRiskEventRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class GetRiskEventUseCaseTest {

    @Test
    void shouldFindRiskEventById() {
        InMemoryRiskEventRepository repository = new InMemoryRiskEventRepository();
        GetRiskEventUseCase useCase = new GetRiskEventUseCase(repository);

        RiskEvent event = createEvent("cus_123");
        RiskResult result = createResult(event, 55, RiskDecision.ALLOW_WITH_MONITORING);

        repository.save(event, result);

        Optional<RiskResult> found = useCase.findById(event.eventId());

        assertTrue(found.isPresent());
        assertEquals(event.eventId(), found.get().eventId());
        assertEquals("cus_123", found.get().customerId());
    }

    @Test
    void shouldReturnEmptyWhenRiskEventDoesNotExist() {
        InMemoryRiskEventRepository repository = new InMemoryRiskEventRepository();
        GetRiskEventUseCase useCase = new GetRiskEventUseCase(repository);

        Optional<RiskResult> found = useCase.findById("missing-id");

        assertTrue(found.isEmpty());
    }

    @Test
    void shouldFindRiskEventsByCustomerId() {
        InMemoryRiskEventRepository repository = new InMemoryRiskEventRepository();
        GetRiskEventUseCase useCase = new GetRiskEventUseCase(repository);

        RiskEvent event1 = createEvent("cus_123");
        RiskEvent event2 = createEvent("cus_123");
        RiskEvent event3 = createEvent("cus_999");

        repository.save(event1, createResult(event1, 10, RiskDecision.ALLOW));
        repository.save(event2, createResult(event2, 60, RiskDecision.REVIEW));
        repository.save(event3, createResult(event3, 100, RiskDecision.BLOCK));

        List<RiskResult> results = useCase.findByCustomerId("cus_123");

        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(result -> result.customerId().equals("cus_123")));
    }

    private RiskEvent createEvent(String customerId) {
        return RiskEvent.newEvent(
                customerId,
                EventType.PIX_CREATED,
                new BigDecimal("1000.00"),
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