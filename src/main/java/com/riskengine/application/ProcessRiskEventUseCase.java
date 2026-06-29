package com.riskengine.application;

import com.riskengine.domain.enums.EventType;
import com.riskengine.domain.enums.RiskDecision;
import com.riskengine.domain.enums.RiskReason;
import com.riskengine.domain.model.RiskEvent;
import com.riskengine.domain.model.RiskResult;
import com.riskengine.domain.repository.RiskEventRepository;
import jakarta.inject.Singleton;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class ProcessRiskEventUseCase {

    private static final BigDecimal HIGH_AMOUNT = new BigDecimal("1000.00");
    private static final BigDecimal VERY_HIGH_AMOUNT = new BigDecimal("5000.00");

    private final RiskEventRepository repository;

    public ProcessRiskEventUseCase(RiskEventRepository repository) {
        this.repository = repository;
    }

    public RiskResult execute(RiskEvent event) {
        RiskAnalysis analysis = analyze(event);

        RiskResult result = new RiskResult(
                event.eventId(),
                event.customerId(),
                event.eventType(),
                analysis.score(),
                decide(analysis.score()),
                analysis.reasons(),
                Instant.now()
        );

        return repository.save(event, result);
    }

    private RiskAnalysis analyze(RiskEvent event) {
        int score = 0;
        List<RiskReason> reasons = new ArrayList<>();

        if (event.amount().compareTo(VERY_HIGH_AMOUNT) >= 0) {
            score += 50;
            reasons.add(RiskReason.VERY_HIGH_AMOUNT);
        } else if (event.amount().compareTo(HIGH_AMOUNT) >= 0) {
            score += 30;
            reasons.add(RiskReason.HIGH_AMOUNT);
        }

        if (event.eventType() == EventType.DEVICE_CHANGED) {
            score += 25;
            reasons.add(RiskReason.DEVICE_CHANGED);
        }

        if (event.eventType() == EventType.LOGIN_SUSPICIOUS) {
            score += 35;
            reasons.add(RiskReason.SUSPICIOUS_LOGIN);
        }

        if (event.eventType() == EventType.ACCOUNT_RISK_UPDATED) {
            score += 20;
            reasons.add(RiskReason.ACCOUNT_RISK_UPDATED);
        }

        if ("mobile".equalsIgnoreCase(event.channel())) {
            score += 10;
            reasons.add(RiskReason.MOBILE_CHANNEL);
        }

        String ipRisk = event.metadata().getOrDefault("ipRisk", "LOW");

        if ("MEDIUM".equalsIgnoreCase(ipRisk)) {
            score += 15;
            reasons.add(RiskReason.MEDIUM_IP_RISK);
        }

        if ("HIGH".equalsIgnoreCase(ipRisk)) {
            score += 30;
            reasons.add(RiskReason.HIGH_IP_RISK);
        }

        return new RiskAnalysis(Math.min(score, 100), List.copyOf(reasons));
    }

    private RiskDecision decide(int score) {
        if (score >= 85) {
            return RiskDecision.BLOCK;
        }

        if (score >= 60) {
            return RiskDecision.REVIEW;
        }

        if (score >= 35) {
            return RiskDecision.ALLOW_WITH_MONITORING;
        }

        return RiskDecision.ALLOW;
    }

    private record RiskAnalysis(int score, List<RiskReason> reasons) {
    }
}