package com.riskengine.api;

import com.riskengine.application.GetRiskEventUseCase;
import com.riskengine.application.ProcessRiskEventUseCase;
import com.riskengine.application.dto.CreateRiskEventRequest;
import com.riskengine.domain.model.RiskEvent;
import com.riskengine.domain.model.RiskResult;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import jakarta.validation.Valid;

import java.util.List;

@Controller("/risk-events")
public class RiskEventController {

    private final ProcessRiskEventUseCase processRiskEventUseCase;
    private final GetRiskEventUseCase getRiskEventUseCase;

    public RiskEventController(
            ProcessRiskEventUseCase processRiskEventUseCase,
            GetRiskEventUseCase getRiskEventUseCase
    ) {
        this.processRiskEventUseCase = processRiskEventUseCase;
        this.getRiskEventUseCase = getRiskEventUseCase;
    }

    @Post
    public HttpResponse<RiskResult> create(@Body @Valid CreateRiskEventRequest request) {
        RiskEvent event = RiskEvent.newEvent(
                request.customerId(),
                request.eventType(),
                request.amount(),
                request.deviceId(),
                request.channel(),
                request.metadata()
        );

        RiskResult result = processRiskEventUseCase.execute(event);

        return HttpResponse.created(result);
    }

    @Get("/{eventId}")
    public HttpResponse<RiskResult> findById(String eventId) {
        return getRiskEventUseCase.findById(eventId)
                .map(HttpResponse::ok)
                .orElseGet(HttpResponse::notFound);
    }

    @Get("/customer/{customerId}")
    public HttpResponse<List<RiskResult>> findByCustomerId(String customerId) {
        return HttpResponse.ok(getRiskEventUseCase.findByCustomerId(customerId));
    }
}