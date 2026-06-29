package com.riskengine.domain.repository;

import com.riskengine.domain.model.RiskEvent;
import com.riskengine.domain.model.RiskResult;

import java.util.List;
import java.util.Optional;

public interface RiskEventRepository {

    RiskResult save(RiskEvent event, RiskResult result);

    Optional<RiskResult> findById(String eventId);

    List<RiskResult> findByCustomerId(String customerId);
}