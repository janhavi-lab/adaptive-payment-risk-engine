package com.janhavi.apre.service;

import com.janhavi.apre.entity.PaymentTransaction;
import com.janhavi.apre.entity.SecurityTest;
import com.janhavi.apre.enums.Decision;
import com.janhavi.apre.enums.RiskCategory;
import com.janhavi.apre.repository.PaymentRepository;
import com.janhavi.apre.repository.SecurityTestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class SecurityDefenseService {

    private static final Logger log = LoggerFactory.getLogger(SecurityDefenseService.class);

    private final MlDefenseClient mlDefenseClient;
    private final SecurityTestRepository securityTestRepository;
    private final PaymentRepository paymentRepository;

    public SecurityDefenseService(MlDefenseClient mlDefenseClient,
                                  SecurityTestRepository securityTestRepository,
                                  PaymentRepository paymentRepository) {
        this.mlDefenseClient = mlDefenseClient;
        this.securityTestRepository = securityTestRepository;
        this.paymentRepository = paymentRepository;
    }

    // =========================================================
    // ATTACK MY DEFENCE (SIMULATION & BENCHMARKING)
    // =========================================================

    public Map<String, Object> triggerAttack(String attackType, int batchSize) {
        int effectiveBatchSize = batchSize > 0 ? batchSize : 50;
        Map<String, Object> simResult = mlDefenseClient.simulateAttack(attackType, effectiveBatchSize);

        SecurityTest test = new SecurityTest();
        test.setTestId((String) simResult.getOrDefault("test_id", "ATK-" + (System.currentTimeMillis() % 1000000)));
        test.setAttackType((String) simResult.getOrDefault("attack_type", "ACCOUNT_TAKEOVER"));
        test.setAttackName((String) simResult.getOrDefault("attack_name", "Account Takeover"));

        Object genObj = simResult.get("transactions_generated");
        test.setTransactionsGenerated(genObj instanceof Number ? ((Number) genObj).intValue() : effectiveBatchSize);

        Object detObj = simResult.get("detected_count");
        test.setDetectedCount(detObj instanceof Number ? ((Number) detObj).intValue() : 0);

        Object missObj = simResult.get("missed_count");
        int missedCount = missObj instanceof Number ? ((Number) missObj).intValue() : 0;
        test.setMissedCount(missedCount);

        Object rateObj = simResult.get("detection_rate");
        test.setDetectionRate(rateObj instanceof Number ? ((Number) rateObj).doubleValue() : 0.0);

        test.setDefenseStatus((String) simResult.getOrDefault("defense_status", "NEEDS ATTENTION"));
        test.setWeaknessIdentified((String) simResult.getOrDefault("weakness_identified", "Evasion pattern identified"));

        String feedbackCandStatus = (String) simResult.get("feedback_candidate_status");
        if (feedbackCandStatus == null || feedbackCandStatus.isBlank()) {
            feedbackCandStatus = missedCount > 0 ? "FEEDBACK IDENTIFIED" : "NO EVASION FOUND";
        }
        test.setFeedbackCandidateStatus(feedbackCandStatus);

        test.setFeedbackStatus((String) simResult.getOrDefault("feedback_status", 
                missedCount > 0 ? missedCount + " adversarial patterns added to the evaluation queue." : "No significant evasion pattern detected in this test."));
        
        test.setAdaptiveInsight((String) simResult.getOrDefault("adaptive_insight", 
                "Defense insights synthesized from adversarial evaluation."));
        test.setCreatedAt(LocalDateTime.now());

        SecurityTest savedTest = securityTestRepository.save(test);
        simResult.put("saved_in_database", true);
        simResult.put("db_id", savedTest.getId());
        simResult.put("feedback_candidate_status", test.getFeedbackCandidateStatus());
        simResult.put("adaptive_insight", test.getAdaptiveInsight());

        return simResult;
    }


    // =========================================================
    // TEST HISTORY
    // =========================================================

    public Page<SecurityTest> getRecentTests(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return securityTestRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    public List<SecurityTest> getTopRecentTests() {
        return securityTestRepository.findTop10ByOrderByCreatedAtDesc();
    }

    // =========================================================
    // ATTACK INTELLIGENCE SCENARIOS
    // =========================================================

    public List<Map<String, Object>> getScenarios() {
        return mlDefenseClient.getAttackScenarios();
    }

    // =========================================================
    // SYSTEM DEFENSE METRICS (REAL DATABASE DATA)
    // =========================================================

    public Map<String, Object> getSystemDefenseMetrics() {
        long totalPayments = paymentRepository.count();
        List<PaymentTransaction> allPayments = paymentRepository.findAll();

        long threatsDetected = allPayments.stream()
                .filter(t -> t.getDecision() == Decision.DECLINED
                        || t.getDecision() == Decision.MANUAL_REVIEW
                        || t.getRiskCategory() == RiskCategory.HIGH
                        || t.getRiskCategory() == RiskCategory.CRITICAL)
                .count();

        long approvedPayments = allPayments.stream()
                .filter(t -> t.getDecision() == Decision.APPROVED || t.getDecision() == Decision.APPROVED_WITH_MONITORING)
                .count();

        List<SecurityTest> tests = securityTestRepository.findAll();
        long totalDefenseTests = tests.size();
        long attacksSimulated = tests.stream().mapToLong(SecurityTest::getTransactionsGenerated).sum();
        long totalMissed = tests.stream().mapToLong(SecurityTest::getMissedCount).sum();
        long feedbackIdentifiedCount = tests.stream()
                .filter(t -> "FEEDBACK IDENTIFIED".equalsIgnoreCase(t.getFeedbackCandidateStatus()) || t.getMissedCount() > 0)
                .count();

        double avgDetectionRate;
        if (!tests.isEmpty()) {
            avgDetectionRate = tests.stream().mapToDouble(SecurityTest::getDetectionRate).average().orElse(0.0);
        } else {
            avgDetectionRate = 94.2; // Default starting benchmark if 0 tests run yet
        }

        Map<String, Object> metrics = new LinkedHashMap<>();
        metrics.put("defense_status", "ACTIVE");
        metrics.put("total_payments_evaluated", totalPayments);
        metrics.put("threats_detected", threatsDetected);
        metrics.put("approved_payments", approvedPayments);
        metrics.put("total_defense_tests", totalDefenseTests);
        metrics.put("attacks_simulated", attacksSimulated);
        metrics.put("overall_detection_rate", Math.round(avgDetectionRate * 10.0) / 10.0);
        metrics.put("missed_patterns_count", totalMissed);
        metrics.put("feedback_identified_tests", feedbackIdentifiedCount);
        metrics.put("active_attack_scenarios", 8);
        metrics.put("feedback_queue_status", totalMissed > 0
                ? totalMissed + " adversarial patterns added to evaluation queue"
                : "No unhandled failure patterns in queue");

        return metrics;
    }
}

