package com.janhavi.apre.service;

import com.janhavi.apre.dto.PaymentRequest;
import com.janhavi.apre.dto.PaymentResponse;
import com.janhavi.apre.entity.PaymentTransaction;
import com.janhavi.apre.enums.Decision;
import com.janhavi.apre.enums.RiskCategory;
import com.janhavi.apre.mapper.PaymentMapper;
import com.janhavi.apre.repository.PaymentRepository;
import com.janhavi.apre.rules.RiskResult;
import com.janhavi.apre.rules.RiskRule;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RiskEngineService {

    private final List<RiskRule> rules;
    private final PaymentRepository paymentRepository;
    private final MlDefenseClient mlDefenseClient;

    public RiskEngineService(
            List<RiskRule> rules,
            PaymentRepository paymentRepository,
            MlDefenseClient mlDefenseClient) {

        this.rules = rules;
        this.paymentRepository = paymentRepository;
        this.mlDefenseClient = mlDefenseClient;
    }

    // =========================================================
    // PAYMENT EVALUATION
    // =========================================================

    public PaymentResponse evaluate(PaymentRequest request) {

        // =====================================================
        // 1. EXISTING JAVA RULE ENGINE
        // =====================================================

        RiskResult ruleResult = new RiskResult();

        for (RiskRule rule : rules) {

            rule.evaluate(
                    request,
                    ruleResult
            );
        }

        int ruleRiskScore =
                ruleResult.getScore();


        // =====================================================
        // 2. PYTHON ML DEFENSE
        // =====================================================

        MlDefenseClient.MlDefenseResult mlResult =
                mlDefenseClient.evaluate(request);


        // =====================================================
        // 3. GET ML RISK SCORE
        // =====================================================

        int mlRiskScore =
                (int) Math.round(
                        mlResult.getRiskScore()
                );


        // =====================================================
        // 4. COMBINE JAVA + ML RISK
        // =====================================================

        /*
         * We use the stronger risk signal.
         *
         * Example:
         *
         * Java rules = 30
         * ML defense = 93
         *
         * Final = 93
         *
         * This prevents a high ML risk from being hidden
         * by a low Java-rule score.
         */

        int finalRiskScore =
                Math.max(
                        ruleRiskScore,
                        mlRiskScore
                );

        // Keep score between 0 and 100

        finalRiskScore =
                Math.max(
                        0,
                        Math.min(
                                100,
                                finalRiskScore
                        )
                );


        // =====================================================
        // 5. DETERMINE RISK CATEGORY
        // =====================================================

        RiskCategory riskCategory;

        if (finalRiskScore < 20) {

            riskCategory =
                    RiskCategory.LOW;

        } else if (finalRiskScore < 50) {

            riskCategory =
                    RiskCategory.MEDIUM;

        } else if (finalRiskScore < 80) {

            riskCategory =
                    RiskCategory.HIGH;

        } else {

            riskCategory =
                    RiskCategory.CRITICAL;
        }


        // =====================================================
        // 6. DETERMINE JAVA DECISION
        // =====================================================

        Decision decision;

        if (riskCategory == RiskCategory.LOW) {

            decision =
                    Decision.APPROVED;

        } else if (riskCategory == RiskCategory.MEDIUM) {

            decision =
                    Decision.APPROVED_WITH_MONITORING;

        } else if (riskCategory == RiskCategory.HIGH) {

            decision =
                    Decision.MANUAL_REVIEW;

        } else {

            decision =
                    Decision.DECLINED;
        }


        // =====================================================
        // 7. SYNTHESIZE UNIFIED EXPLAINABLE SIGNALS
        // =====================================================

        List<String> reasons = new ArrayList<>();

        for (String ruleReason : ruleResult.getReasons()) {
            if (ruleReason != null && !ruleReason.isBlank()) {
                reasons.add(ruleReason);
            }
        }

        if (mlResult != null && mlResult.getRiskScore() >= 40.0 && mlResult.getReason() != null && !mlResult.getReason().isBlank()) {
            reasons.add("Behavioral anomaly: " + mlResult.getReason());
        }

        if (reasons.isEmpty()) {
            reasons.add("Transaction attributes and behavioral patterns match verified baseline.");
        }

        String recommendedAction = switch (decision) {
            case APPROVED -> "APPROVE_TRANSACTION";
            case APPROVED_WITH_MONITORING -> "APPROVE_WITH_MONITORING";
            case MANUAL_REVIEW -> "MANUAL_REVIEW";
            case DECLINED -> "BLOCK_TRANSACTION";
        };

        // =====================================================
        // 8. BUILD RESPONSE (ADAPTIVE RISK ORCHESTRATOR)
        // =====================================================

        PaymentResponse response = new PaymentResponse();
        response.setRiskScore(finalRiskScore);
        response.setRiskCategory(riskCategory);
        response.setDecision(decision);
        response.setRecommendedAction(recommendedAction);
        response.setReasons(reasons);

        // =====================================================
        // 9. SAVE TRANSACTION
        // =====================================================

        PaymentTransaction transaction =
                PaymentMapper.toEntity(
                        request,
                        response
                );

        paymentRepository.save(
                transaction
        );

        // =====================================================
        // 10. RETURN TRANSACTION ID
        // =====================================================

        response.setTransactionId(
                transaction.getTransactionId()
        );

        return response;
    }


    // =========================================================
    // FETCH TRANSACTIONS
    // =========================================================

    public Page<PaymentTransaction> getTransactions(
            int page,
            int size,
            String sortBy,
            String direction,
            RiskCategory riskCategory,
            Decision decision) {

        Pageable pageable =
                PageRequest.of(
                        page,
                        size,
                        direction.equalsIgnoreCase("asc")
                                ? Sort.by(sortBy).ascending()
                                : Sort.by(sortBy).descending()
                );


        // -----------------------------------------------------
        // FILTER BY RISK CATEGORY
        // -----------------------------------------------------

        if (riskCategory != null) {

            return paymentRepository
                    .findByRiskCategory(
                            riskCategory,
                            pageable
                    );
        }


        // -----------------------------------------------------
        // FILTER BY DECISION
        // -----------------------------------------------------

        if (decision != null) {

            return paymentRepository
                    .findByDecision(
                            decision,
                            pageable
                    );
        }


        // -----------------------------------------------------
        // RETURN ALL
        // -----------------------------------------------------

        return paymentRepository.findAll(
                pageable
        );
    }
}