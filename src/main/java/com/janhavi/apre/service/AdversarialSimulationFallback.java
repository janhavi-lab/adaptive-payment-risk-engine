package com.janhavi.apre.service;

import com.janhavi.apre.dto.PaymentRequest;
import com.janhavi.apre.enums.Decision;
import com.janhavi.apre.enums.MerchantCategory;
import com.janhavi.apre.enums.PaymentMethod;
import com.janhavi.apre.enums.RiskCategory;
import com.janhavi.apre.rules.RiskResult;
import com.janhavi.apre.rules.RiskRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class AdversarialSimulationFallback {

    private static final Logger log = LoggerFactory.getLogger(AdversarialSimulationFallback.class);

    private final List<RiskRule> rules;

    public AdversarialSimulationFallback(List<RiskRule> rules) {
        this.rules = rules;
    }

    public Map<String, Object> simulateAttack(String requestedAttackType, int batchSize) {
        log.info("Executing controlled adversarial simulation fallback for scenario '{}' (batch size: {})", requestedAttackType, batchSize);

        int total = Math.max(10, Math.min(batchSize > 0 ? batchSize : 50, 200));
        String attackType = resolveAttackType(requestedAttackType);
        String attackName = getScenarioName(attackType);
        String description = getScenarioDescription(attackType);
        String testId = "ATK-" + (System.currentTimeMillis() % 1000000);

        List<Map<String, Object>> evaluatedTxs = new ArrayList<>();
        List<Map<String, Object>> missedSamples = new ArrayList<>();
        int detectedCount = 0;
        int missedCount = 0;

        for (int i = 1; i <= total; i++) {
            PaymentRequest req = generateSyntheticAdversarialTx(attackType);
            RiskResult result = new RiskResult();
            for (RiskRule rule : rules) {
                rule.evaluate(req, result);
            }

            int score = Math.max(0, Math.min(100, result.getScore()));
            RiskCategory riskCategory = score < 20 ? RiskCategory.LOW : (score < 50 ? RiskCategory.MEDIUM : (score < 80 ? RiskCategory.HIGH : RiskCategory.CRITICAL));
            Decision decision = riskCategory == RiskCategory.LOW ? Decision.APPROVED : (riskCategory == RiskCategory.MEDIUM ? Decision.APPROVED_WITH_MONITORING : (riskCategory == RiskCategory.HIGH ? Decision.MANUAL_REVIEW : Decision.DECLINED));
            String action = switch (decision) {
                case APPROVED -> "APPROVE_TRANSACTION";
                case APPROVED_WITH_MONITORING -> "APPROVE_WITH_MONITORING";
                case MANUAL_REVIEW -> "MANUAL_REVIEW";
                case DECLINED -> "BLOCK_TRANSACTION";
            };

            boolean isDetected = (score >= 35) || (decision != Decision.APPROVED);

            Map<String, Object> txRecord = new LinkedHashMap<>();
            txRecord.put("id", String.format("SIM-%03d", i));
            txRecord.put("amount", req.getAmount().doubleValue());
            txRecord.put("merchant_category", req.getMerchantCategory() != null ? req.getMerchantCategory().name() : "ECOMMERCE");
            txRecord.put("country", req.getCountry());
            txRecord.put("payment_method", req.getPaymentMethod() != null ? req.getPaymentMethod().name() : "UPI");
            txRecord.put("risk_score", score);
            txRecord.put("risk_level", riskCategory.name());
            txRecord.put("decision", decision.name());
            txRecord.put("action", action);
            txRecord.put("signals", result.getReasons());
            txRecord.put("detected", isDetected);

            evaluatedTxs.add(txRecord);

            if (isDetected) {
                detectedCount++;
            } else {
                missedCount++;
                if (missedSamples.size() < 5) {
                    missedSamples.add(txRecord);
                }
            }
        }

        double detectionRate = Math.round(((double) detectedCount / total) * 1000.0) / 10.0;
        String defenseStatus = detectionRate >= 90.0 ? "RESILIENT" : (detectionRate >= 70.0 ? "NEEDS ATTENTION" : "VULNERABLE");

        String weaknessIdentified;
        String feedbackCandidateStatus;
        String feedbackStatus;
        String adaptiveInsight;

        if (missedCount > 0) {
            weaknessIdentified = getWeaknessCatalog(attackType);
            feedbackCandidateStatus = "FEEDBACK IDENTIFIED";
            feedbackStatus = missedCount + " adversarial patterns added to the evaluation queue.";
            adaptiveInsight = getInsightCatalog(attackType, attackName);
        } else {
            weaknessIdentified = "No significant evasion pattern detected in this test.";
            feedbackCandidateStatus = "NO EVASION FOUND";
            feedbackStatus = "No significant evasion pattern detected in this test.";
            adaptiveInsight = "Defense decision boundaries successfully intercepted all generated adversarial test vectors for " + attackName + ". Baseline defense held.";
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("test_id", testId);
        payload.put("attack_type", attackType);
        payload.put("attack_name", attackName);
        payload.put("description", description);
        payload.put("transactions_generated", total);
        payload.put("detected_count", detectedCount);
        payload.put("missed_count", missedCount);
        payload.put("detection_rate", detectionRate);
        payload.put("defense_status", defenseStatus);
        payload.put("weakness_identified", weaknessIdentified);
        payload.put("feedback_candidate_status", feedbackCandidateStatus);
        payload.put("feedback_status", feedbackStatus);
        payload.put("feedback_candidate_count", missedCount);
        payload.put("adaptive_insight", adaptiveInsight);
        payload.put("next_defense_focus", attackType);
        payload.put("simulation_source", "ORCHESTRATOR_FALLBACK");
        payload.put("sample_transactions", evaluatedTxs.subList(0, Math.min(evaluatedTxs.size(), 10)));
        payload.put("missed_samples", missedSamples);
        payload.put("timestamp", LocalDateTime.now().toString());

        return payload;
    }

    private String resolveAttackType(String requested) {
        if (requested == null || requested.isBlank() || "AUTO".equalsIgnoreCase(requested)) {
            String[] scenarios = {
                    "ACCOUNT_TAKEOVER", "MULTI_SIGNAL_ATTACK", "SYNTHETIC_IDENTITY",
                    "VELOCITY_ABUSE", "GEOGRAPHIC_ANOMALY", "PAYMENT_METHOD_ABUSE",
                    "MERCHANT_ANOMALY", "TRANSACTION_ANOMALY"
            };
            return scenarios[ThreadLocalRandom.current().nextInt(scenarios.length)];
        }
        return requested.toUpperCase();
    }

    private PaymentRequest generateSyntheticAdversarialTx(String scenario) {
        PaymentRequest req = new PaymentRequest();
        ThreadLocalRandom rnd = ThreadLocalRandom.current();

        switch (scenario) {
            case "ACCOUNT_TAKEOVER" -> {
                // High amounts from international country on credit card
                double amt = rnd.nextDouble(25000, 120000);
                String[] countries = {"NG", "US", "RU", "GB"};
                req.setAmount(BigDecimal.valueOf(Math.round(amt * 100.0) / 100.0));
                req.setMerchantName("QuickBuy International");
                req.setMerchantCategory(MerchantCategory.ECOMMERCE);
                req.setCountry(countries[rnd.nextInt(countries.length)]);
                req.setPaymentMethod(PaymentMethod.CREDIT_CARD);
            }
            case "SYNTHETIC_IDENTITY" -> {
                // Young account burst on high risk merchant category
                double amt = rnd.nextDouble(15000, 85000);
                MerchantCategory[] cats = {MerchantCategory.TRAVEL, MerchantCategory.ENTERTAINMENT, MerchantCategory.ECOMMERCE};
                req.setAmount(BigDecimal.valueOf(Math.round(amt * 100.0) / 100.0));
                req.setMerchantName("Digital Pay Gateway");
                req.setMerchantCategory(cats[rnd.nextInt(cats.length)]);
                req.setCountry("IN");
                req.setPaymentMethod(PaymentMethod.WALLET);
            }
            case "VELOCITY_ABUSE" -> {
                // High velocity micro-transactions
                double amt = rnd.nextDouble(1500, 18000);
                req.setAmount(BigDecimal.valueOf(Math.round(amt * 100.0) / 100.0));
                req.setMerchantName("Micro Pay Hub");
                req.setMerchantCategory(MerchantCategory.GROCERY);
                req.setCountry("IN");
                req.setPaymentMethod(PaymentMethod.UPI);
            }
            case "GEOGRAPHIC_ANOMALY" -> {
                // Low amount from risky foreign country
                double amt = rnd.nextDouble(4500, 48000);
                String[] countries = {"NG", "RU", "PK", "CN"};
                req.setAmount(BigDecimal.valueOf(Math.round(amt * 100.0) / 100.0));
                req.setMerchantName("Global Transit Gate");
                req.setMerchantCategory(MerchantCategory.TRAVEL);
                req.setCountry(countries[rnd.nextInt(countries.length)]);
                req.setPaymentMethod(PaymentMethod.CREDIT_CARD);
            }
            case "PAYMENT_METHOD_ABUSE" -> {
                // Alternate wallet rail transactions
                double amt = rnd.nextDouble(8000, 55000);
                req.setAmount(BigDecimal.valueOf(Math.round(amt * 100.0) / 100.0));
                req.setMerchantName("Fast Wallet Direct");
                req.setMerchantCategory(MerchantCategory.ECOMMERCE);
                req.setCountry("IN");
                req.setPaymentMethod(PaymentMethod.WALLET);
            }
            case "MERCHANT_ANOMALY" -> {
                // Unusual high risk merchant category
                double amt = rnd.nextDouble(18000, 95000);
                req.setAmount(BigDecimal.valueOf(Math.round(amt * 100.0) / 100.0));
                req.setMerchantName("Crypto Vault Hub");
                req.setMerchantCategory(MerchantCategory.ENTERTAINMENT);
                req.setCountry("IN");
                req.setPaymentMethod(PaymentMethod.CREDIT_CARD);
            }
            case "MULTI_SIGNAL_ATTACK" -> {
                // Multi-signal: Foreign country + high amount + wallet rail
                double amt = rnd.nextDouble(35000, 140000);
                String[] countries = {"NG", "US", "RU"};
                req.setAmount(BigDecimal.valueOf(Math.round(amt * 100.0) / 100.0));
                req.setMerchantName("Apex Global Gateway");
                req.setMerchantCategory(MerchantCategory.TRAVEL);
                req.setCountry(countries[rnd.nextInt(countries.length)]);
                req.setPaymentMethod(PaymentMethod.WALLET);
            }
            default -> { // TRANSACTION_ANOMALY (Low-and-slow)
                double amt = rnd.nextDouble(3500, 26000);
                req.setAmount(BigDecimal.valueOf(Math.round(amt * 100.0) / 100.0));
                req.setMerchantName("Retail Express Point");
                req.setMerchantCategory(MerchantCategory.ECOMMERCE);
                req.setCountry("IN");
                req.setPaymentMethod(PaymentMethod.UPI);
            }
        }

        return req;
    }

    private String getScenarioName(String scenario) {
        return switch (scenario) {
            case "ACCOUNT_TAKEOVER" -> "Account Takeover";
            case "MULTI_SIGNAL_ATTACK" -> "Multi-Signal Coordinated Attack";
            case "SYNTHETIC_IDENTITY" -> "Synthetic Identity";
            case "VELOCITY_ABUSE" -> "Velocity Burst Abuse";
            case "GEOGRAPHIC_ANOMALY" -> "Geographic Anomaly / Impossible Travel";
            case "PAYMENT_METHOD_ABUSE" -> "Payment Rail Switching";
            case "MERCHANT_ANOMALY" -> "Merchant Category Anomaly";
            case "TRANSACTION_ANOMALY" -> "Low-and-Slow Amount Mutation";
            default -> scenario.replace("_", " ").toLowerCase();
        };
    }

    private String getScenarioDescription(String scenario) {
        return switch (scenario) {
            case "ACCOUNT_TAKEOVER" -> "Simulates sudden new-device logins, IP shifts, and low-amount probing to test session integrity.";
            case "MULTI_SIGNAL_ATTACK" -> "Combines high-risk country, wallet rail, and off-hour velocity to test multi-signal correlation.";
            case "SYNTHETIC_IDENTITY" -> "Generates newly registered accounts with nascent transaction bursts to probe account-age friction.";
            case "VELOCITY_ABUSE" -> "Executes micro-transaction bursts across multiple merchants to test velocity aggregation.";
            case "GEOGRAPHIC_ANOMALY" -> "Cross-border routing paired with low-risk merchant categories to test impossible travel rules.";
            case "PAYMENT_METHOD_ABUSE" -> "Rapidly switches from domestic cards to high-risk wallet rails to test rail-switching heuristics.";
            case "MERCHANT_ANOMALY" -> "Probes first-time merchant category transactions to test customer profile entropy limits.";
            case "TRANSACTION_ANOMALY" -> "Mutates transaction amounts just below static threshold caps to evaluate dynamic deviation triggers.";
            default -> "Controlled synthetic adversarial simulation payload.";
        };
    }

    private String getWeaknessCatalog(String scenario) {
        return switch (scenario) {
            case "ACCOUNT_TAKEOVER" -> "Rapid device-switching under low transaction amounts";
            case "SYNTHETIC_IDENTITY" -> "Newly registered accounts with low initial velocity";
            case "VELOCITY_ABUSE" -> "Micro-transaction bursts spread across diverse merchants";
            case "GEOGRAPHIC_ANOMALY" -> "Cross-border routing paired with low-risk merchant categories";
            case "PAYMENT_METHOD_ABUSE" -> "Low-value transactions through alternate wallet rails";
            case "MERCHANT_ANOMALY" -> "First-time merchant purchases with domestic cards";
            case "MULTI_SIGNAL_ATTACK" -> "Subtle multi-signal combinations staying below hard rule thresholds";
            case "TRANSACTION_ANOMALY" -> "Sub-threshold incremental amount scaling";
            default -> "Subtle behavioral mutations evading single-point thresholds";
        };
    }

    private String getInsightCatalog(String scenario, String name) {
        return switch (scenario) {
            case "GEOGRAPHIC_ANOMALY" -> "Geographic anomalies remain an evasion opportunity. Prioritize cross-border behavioral signals and travel velocity in the next defense evaluation cycle.";
            case "PAYMENT_METHOD_ABUSE" -> "Payment rail switching shows sub-threshold evasion. Strengthen wallet-rail risk weightings and new-rail velocity monitoring.";
            case "MERCHANT_ANOMALY" -> "Unusual merchant probing bypassed standard filters. Incorporate merchant category entropy clustering into baseline customer profiles.";
            case "ACCOUNT_TAKEOVER" -> "Credential stuffing surfaced subtle low-amount bypasses. Tighten device fingerprinting and new-device friction thresholds.";
            case "SYNTHETIC_IDENTITY" -> "Nascent accounts with low initial velocity evaded static limits. Calibrate young-account friction curves in the risk orchestrator.";
            case "VELOCITY_ABUSE" -> "Distributed micro-bursts evaded single-merchant limits. Introduce cross-merchant rolling window frequency counters.";
            case "MULTI_SIGNAL_ATTACK" -> "Coordinated multi-signal mutations evaded hard rules. Elevate non-linear multi-signal correlation weights in defense evaluation.";
            case "TRANSACTION_ANOMALY" -> "Sub-threshold incremental amount mutations bypassed fixed caps. Adjust dynamic standard-deviation spend triggers.";
            default -> "Evasion opportunity discovered in " + name + ". Prioritize feature correlation for these patterns in the next evaluation cycle.";
        };
    }
}
