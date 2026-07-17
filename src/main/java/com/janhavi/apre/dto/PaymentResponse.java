package com.janhavi.apre.dto;

import com.janhavi.apre.enums.Decision;
import com.janhavi.apre.enums.RiskCategory;

import java.util.List;
import java.util.UUID;

public class PaymentResponse {

    private UUID transactionId;

    private Integer riskScore;

    private RiskCategory riskCategory;

    private Decision decision;

    private List<String> reasons;

    public PaymentResponse() {
    }

    public UUID getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(UUID transactionId) {
        this.transactionId = transactionId;
    }

    public Integer getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(Integer riskScore) {
        this.riskScore = riskScore;
    }

    public RiskCategory getRiskCategory() {
        return riskCategory;
    }

    public void setRiskCategory(RiskCategory riskCategory) {
        this.riskCategory = riskCategory;
    }

    public Decision getDecision() {
        return decision;
    }

    public void setDecision(Decision decision) {
        this.decision = decision;
    }

    public List<String> getReasons() {
        return reasons;
    }

    public void setReasons(List<String> reasons) {
        this.reasons = reasons;
    }
}