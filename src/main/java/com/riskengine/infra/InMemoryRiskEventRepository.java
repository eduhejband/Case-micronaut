package com.riskengine.infra;

import com.riskengine.domain.model.RiskEvent;
import com.riskengine.domain.model.RiskResult;
import com.riskengine.domain.repository.RiskEventRepository;
import jakarta.inject.Singleton;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class InMemoryRiskEventRepository implements RiskEventRepository {

    private final ConcurrentHashMap<String, RiskResult> storage = new ConcurrentHashMap<>();

    @Override
    public RiskResult save(RiskEvent event, RiskResult result) {
        storage.put(event.eventId(), result);
        return result;
    }

    @Override
    public Optional<RiskResult> findById(String eventId) {
        return Optional.ofNullable(storage.get(eventId));
    }

    @Override
    public List<RiskResult> findByCustomerId(String customerId) {
        return storage.values()
                .stream()
                .filter(result -> result.customerId().equals(customerId))
                .sorted(Comparator.comparing(RiskResult::processedAt).reversed())
                .toList();
    }
}