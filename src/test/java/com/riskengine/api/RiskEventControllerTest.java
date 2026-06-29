package com.riskengine.api;

import com.riskengine.application.GetRiskEventUseCase;
import com.riskengine.application.ProcessRiskEventUseCase;
import com.riskengine.application.dto.CreateRiskEventRequest;
import com.riskengine.domain.enums.EventType;
import com.riskengine.domain.enums.RiskDecision;
import com.riskengine.domain.model.RiskResult;
import com.riskengine.infra.InMemoryRiskEventRepository;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RiskEventControllerTest {

    @Test
    void shouldCreateRiskEvent() {
        RiskEventController controller = createController();

        CreateRiskEventRequest request =
                new CreateRiskEventRequest(
                        "cus_123",
                        EventType.PIX_CREATED,
                        new BigDecimal("1250.90"),
                        "dev_abc",
                        "mobile",
                        Map.of("ipRisk", "MEDIUM")
                );

        HttpResponse<RiskResult> response = controller.create(request);

        assertEquals(HttpStatus.CREATED, response.getStatus());

        RiskResult body = response.getBody().orElseThrow();

        assertNotNull(body.eventId());
        assertEquals("cus_123", body.customerId());
        assertEquals(EventType.PIX_CREATED, body.eventType());
        assertEquals(55, body.riskScore());
        assertEquals(RiskDecision.ALLOW_WITH_MONITORING, body.decision());
    }

    @Test
    void shouldFindRiskEventById() {
        RiskEventController controller = createController();

        CreateRiskEventRequest request =
                new CreateRiskEventRequest(
                        "cus_123",
                        EventType.PIX_CREATED,
                        new BigDecimal("1250.90"),
                        "dev_abc",
                        "mobile",
                        Map.of("ipRisk", "MEDIUM")
                );

        RiskResult created = controller.create(request)
                .getBody()
                .orElseThrow();

        HttpResponse<RiskResult> response = controller.findById(created.eventId());

        assertEquals(HttpStatus.OK, response.getStatus());

        RiskResult found = response.getBody().orElseThrow();

        assertEquals(created.eventId(), found.eventId());
        assertEquals("cus_123", found.customerId());
    }

    @Test
    void shouldReturnNotFoundWhenRiskEventDoesNotExist() {
        RiskEventController controller = createController();

        HttpResponse<RiskResult> response = controller.findById("missing-id");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatus());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    void shouldFindRiskEventsByCustomerId() {
        RiskEventController controller = createController();

        controller.create(new CreateRiskEventRequest(
                "cus_123",
                EventType.PIX_CREATED,
                new BigDecimal("1250.90"),
                "dev_abc",
                "mobile",
                Map.of("ipRisk", "MEDIUM")
        ));

        controller.create(new CreateRiskEventRequest(
                "cus_123",
                EventType.LOGIN_SUSPICIOUS,
                new BigDecimal("500.00"),
                "dev_xyz",
                "web",
                Map.of("ipRisk", "HIGH")
        ));

        HttpResponse<List<RiskResult>> response = controller.findByCustomerId("cus_123");

        assertEquals(HttpStatus.OK, response.getStatus());

        List<RiskResult> body = response.getBody().orElseThrow();

        assertEquals(2, body.size());
        assertTrue(body.stream().allMatch(result -> result.customerId().equals("cus_123")));
    }

    private RiskEventController createController() {
        InMemoryRiskEventRepository repository = new InMemoryRiskEventRepository();

        ProcessRiskEventUseCase processRiskEventUseCase =
                new ProcessRiskEventUseCase(repository);

        GetRiskEventUseCase getRiskEventUseCase =
                new GetRiskEventUseCase(repository);

        return new RiskEventController(processRiskEventUseCase, getRiskEventUseCase);
    }
}