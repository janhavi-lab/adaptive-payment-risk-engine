package com.janhavi.apre.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.janhavi.apre.dto.PaymentRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class MlDefenseClient {

    private static final Logger log = LoggerFactory.getLogger(MlDefenseClient.class);

    private static final String ML_API_URL =
            System.getenv().getOrDefault(
                    "ML_API_URL",
                    "http://127.0.0.1:8001/evaluate"
            );

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final AdversarialSimulationFallback adversarialSimulationFallback;

    public MlDefenseClient(AdversarialSimulationFallback adversarialSimulationFallback) {
        this.adversarialSimulationFallback = adversarialSimulationFallback;
        this.httpClient =
                HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(5))
                        .build();

        this.objectMapper = new ObjectMapper();
    }

    private String getBaseUrl() {
        String url = ML_API_URL;
        if (url.endsWith("/evaluate")) {
            return url.substring(0, url.length() - "/evaluate".length());
        }
        return url;
    }

    // =========================================================
    // PAYMENT EVALUATION (WITH GRACEFUL FALLBACK)
    // =========================================================

    public MlDefenseResult evaluate(PaymentRequest paymentRequest) {
        if (paymentRequest == null || paymentRequest.getAmount() == null) {
            return fallbackEvaluation(paymentRequest);
        }

        try {
            Map<String, Object> payload = new HashMap<>();
            double amount = paymentRequest.getAmount().doubleValue();

            payload.put("amount", amount);
            payload.put("merchant_category", paymentRequest.getMerchantCategory() != null ? paymentRequest.getMerchantCategory().name() : "ECOMMERCE");
            payload.put("country", paymentRequest.getCountry() != null ? paymentRequest.getCountry() : "IN");
            payload.put("payment_method", paymentRequest.getPaymentMethod() != null ? paymentRequest.getPaymentMethod().name() : "UPI");

            int velocity1h = 1;
            int deviceAgeDays = 365;
            int accountAgeDays = 1000;
            double amountDeviation = Math.max(0.5, Math.min(amount / 2500.0, 2.5));
            double velocityDeviation = 1.0;
            double behavioralDeviationScore = (amountDeviation + velocityDeviation) / 2.0;

            payload.put("velocity_1h", velocity1h);
            payload.put("device_age_days", deviceAgeDays);
            payload.put("account_age_days", accountAgeDays);
            payload.put("amount_deviation_ratio", amountDeviation);
            payload.put("velocity_deviation_ratio", velocityDeviation);
            payload.put("behavioral_deviation_score", behavioralDeviationScore);
            payload.put("payment_method_risk", 0.0);
            payload.put("merchant_anomaly", 0);
            payload.put("location_change", 0);
            payload.put("new_payment_method", 0);

            String json = objectMapper.writeValueAsString(payload);

            HttpRequest httpRequest =
                    HttpRequest.newBuilder()
                            .uri(URI.create(ML_API_URL))
                            .timeout(Duration.ofSeconds(4))
                            .header("Content-Type", "application/json")
                            .header("Accept", "application/json")
                            .POST(HttpRequest.BodyPublishers.ofString(json))
                            .build();

            HttpResponse<String> response =
                    httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                JsonNode node = objectMapper.readTree(response.body());
                if (node != null && !node.isNull()) {
                    double riskScore = Math.max(0.0, Math.min(100.0, node.path("risk_score").asDouble(0.0)));
                    double mlProbability = Math.max(0.0, Math.min(1.0, node.path("ml_probability").asDouble(0.0)));
                    String riskLevel = node.path("risk_level").asText("LOW");
                    String decision = node.path("decision").asText("APPROVE");
                    String action = node.path("action").asText("APPROVE_TRANSACTION");
                    String reason = node.path("reason").asText("Behavioral defense baseline check passed");

                    return new MlDefenseResult(riskScore, mlProbability, riskLevel, decision, action, reason);
                }
            }

            log.warn("ML Defense API returned non-success status: {}. Falling back to internal engine.", response.statusCode());
            return fallbackEvaluation(paymentRequest);

        } catch (Exception e) {
            log.warn("ML Defense API unreachable ({}: {}). Using graceful internal defense fallback.", e.getClass().getSimpleName(), e.getMessage());
            return fallbackEvaluation(paymentRequest);
        }
    }

    private MlDefenseResult fallbackEvaluation(PaymentRequest req) {
        if (req == null || req.getAmount() == null) {
            return new MlDefenseResult(10.0, 0.05, "LOW", "APPROVE", "APPROVE_TRANSACTION", "Standard transaction evaluation");
        }

        double amount = req.getAmount().doubleValue();
        double score = 10.0;
        String reason = "Standard baseline transaction";

        if (amount > 100000) {
            score = 65.0;
            reason = "High value transaction relative to profile";
        } else if (amount > 50000) {
            score = 40.0;
            reason = "Moderate transaction value deviation";
        }

        String level = score >= 80 ? "CRITICAL" : (score >= 60 ? "HIGH" : (score >= 35 ? "MEDIUM" : "LOW"));
        String decision = score >= 80 ? "BLOCK" : (score >= 60 ? "STEP_UP" : (score >= 35 ? "MONITOR" : "APPROVE"));
        String action = score >= 80 ? "BLOCK_TRANSACTION" : (score >= 60 ? "MANUAL_REVIEW" : (score >= 35 ? "APPROVE_WITH_MONITORING" : "APPROVE_TRANSACTION"));

        return new MlDefenseResult(score, score / 100.0, level, decision, action, reason);
    }

    // =========================================================
    // ATTACK MY DEFENCE SIMULATION (REAL CALL TO PYTHON ML)
    // =========================================================

    public Map<String, Object> simulateAttack(String attackType, int batchSize) {
        int effectiveBatchSize = batchSize > 0 ? batchSize : 50;
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("attack_type", attackType != null && !attackType.isBlank() ? attackType : "AUTO");
            body.put("batch_size", effectiveBatchSize);

            String json = objectMapper.writeValueAsString(body);
            String url = getBaseUrl() + "/attack/simulate";

            HttpRequest httpRequest =
                    HttpRequest.newBuilder()
                            .uri(URI.create(url))
                            .timeout(Duration.ofSeconds(15))
                            .header("Content-Type", "application/json")
                            .header("Accept", "application/json")
                            .POST(HttpRequest.BodyPublishers.ofString(json))
                            .build();

            HttpResponse<String> response =
                    httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                Map<String, Object> mlResult = objectMapper.readValue(response.body(), Map.class);
                if (mlResult != null) {
                    mlResult.putIfAbsent("simulation_source", "ML_DEFENSE_LAB");
                    return mlResult;
                }
            }

            log.warn("Python attack simulation returned status: {}. Executing controlled orchestrator fallback.", response.statusCode());
            return adversarialSimulationFallback.simulateAttack(attackType, effectiveBatchSize);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Attack simulation interrupted. Using orchestrator fallback.");
            return adversarialSimulationFallback.simulateAttack(attackType, effectiveBatchSize);
        } catch (Exception e) {
            log.warn("Remote ML simulation unavailable ({}). Executing controlled orchestrator fallback simulation.", e.getMessage());
            return adversarialSimulationFallback.simulateAttack(attackType, effectiveBatchSize);
        }
    }


    // =========================================================
    // ATTACK SCENARIOS CATALOG
    // =========================================================

    public List<Map<String, Object>> getAttackScenarios() {
        try {
            String url = getBaseUrl() + "/attack-scenarios";
            HttpRequest httpRequest =
                    HttpRequest.newBuilder()
                            .uri(URI.create(url))
                            .timeout(Duration.ofSeconds(3))
                            .GET()
                            .build();

            HttpResponse<String> response =
                    httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                JsonNode node = objectMapper.readTree(response.body());
                if (node.has("scenarios")) {
                    return objectMapper.convertValue(node.get("scenarios"), List.class);
                }
            }
        } catch (Exception e) {
            log.info("Could not fetch remote scenarios catalog. Using built-in attack intelligence catalog.");
        }

        return getBuiltInAttackScenarios();
    }

    public List<Map<String, Object>> getBuiltInAttackScenarios() {
        List<Map<String, Object>> list = new ArrayList<>();

        list.add(createScenario("ACCOUNT_TAKEOVER", "Account Takeover",
                "Simulates a transaction inconsistent with customer usual device, location, or velocity.",
                "CRITICAL", "Credential stuffing & new device masquerading",
                List.of("new_device", "location_change", "unusual_amount", "high_velocity"),
                "Tests device intelligence & behavioral baseline anomaly detection"));

        list.add(createScenario("MULTI_SIGNAL_ATTACK", "Multi-Signal Coordinated Attack",
                "Combines multiple subtle suspicious signals to simulate sophisticated adversarial evasion.",
                "CRITICAL", "Cross-channel coordinated evasion below hard limits",
                List.of("unusual_amount", "location_change", "new_device", "unusual_payment_method"),
                "Tests multi-layered risk correlation without single-point spikes"));

        list.add(createScenario("SYNTHETIC_IDENTITY", "Synthetic Identity",
                "Simulates an account with limited behavioral history exhibiting fraudulent spending bursts.",
                "HIGH", "Sparse profile bootstrapping & low history",
                List.of("young_account", "limited_history", "unusual_merchant"),
                "Tests cold-start anomaly detection on nascent accounts"));

        list.add(createScenario("VELOCITY_ABUSE", "Velocity Burst Abuse",
                "Simulates an unusually high number of micro-transactions within a short timeframe.",
                "HIGH", "Rapid micro-transaction bursts",
                List.of("high_velocity", "repeated_merchant", "rapid_payment_activity"),
                "Tests short-window frequency counters and threshold rules"));

        list.add(createScenario("GEOGRAPHIC_ANOMALY", "Geographic Anomaly / Impossible Travel",
                "Simulates payments occurring from unusual foreign locations or proxy tunnels.",
                "HIGH", "Impossible travel & cross-border proxy routing",
                List.of("location_change", "country_change", "travel_impossibility"),
                "Tests IP geolocation and cross-border risk scoring"));

        list.add(createScenario("PAYMENT_METHOD_ABUSE", "Payment Rail Switching Abuse",
                "Simulates transactions switching from customer standard method to higher-risk wallet rails.",
                "MEDIUM", "Channel switching to low-friction rails",
                List.of("new_payment_method", "unusual_payment_method"),
                "Tests cross-rail payment consistency monitoring"));

        list.add(createScenario("MERCHANT_ANOMALY", "Merchant Category Anomaly",
                "Simulates activity involving abnormal merchant categories for the customer profile.",
                "MEDIUM", "Unusual merchant category probing",
                List.of("unusual_merchant", "unusual_merchant_category"),
                "Tests historical merchant entropy and category clustering"));

        list.add(createScenario("TRANSACTION_ANOMALY", "Low-and-Slow Amount Mutation",
                "Simulates gradual amount scaling designed to stay just beneath standard rule limits.",
                "MEDIUM", "Sub-threshold incremental amount scaling",
                List.of("unusual_amount", "amount_deviation"),
                "Tests dynamic spending standard deviations"));

        return list;
    }

    private Map<String, Object> createScenario(String type, String name, String desc, String severity,
                                                String weakness, List<String> signals, String challenge) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("type", type);
        map.put("name", name);
        map.put("description", desc);
        map.put("severity", severity);
        map.put("targeted_weakness", weakness);
        map.put("signals", signals);
        map.put("defense_challenge", challenge);
        return map;
    }

    // =========================================================
    // ML RESULT
    // =========================================================

    public static class MlDefenseResult {
        private final double riskScore;
        private final double mlProbability;
        private final String riskLevel;
        private final String decision;
        private final String action;
        private final String reason;

        public MlDefenseResult(double riskScore, double mlProbability, String riskLevel,
                               String decision, String action, String reason) {
            this.riskScore = riskScore;
            this.mlProbability = mlProbability;
            this.riskLevel = riskLevel;
            this.decision = decision;
            this.action = action;
            this.reason = reason;
        }

        public double getRiskScore() {
            return riskScore;
        }

        public double getMlProbability() {
            return mlProbability;
        }

        public String getRiskLevel() {
            return riskLevel;
        }

        public String getDecision() {
            return decision;
        }

        public String getAction() {
            return action;
        }

        public String getReason() {
            return reason;
        }
    }
}