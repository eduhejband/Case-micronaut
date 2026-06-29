package com.riskengine.application;

import com.riskengine.domain.model.RiskResult;
import com.riskengine.domain.repository.RiskEventRepository;
import jakarta.inject.Singleton;

import java.util.List;
import java.util.Optional;

@Singleton
public class GetRiskEventUseCase {

    private final RiskEventRepository repository;

    public GetRiskEventUseCase(RiskEventRepository repository) {
        this.repository = repository;
    }

    public Optional<RiskResult> findById(String eventId) {
        return repository.findById(eventId);
    }

    public List<RiskResult> findByCustomerId(String customerId) {
        return repository.findByCustomerId(customerId);
    }
}