package com.janhavi.apre.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.janhavi.apre.dto.PaymentRequest;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

@Service
public class MlDefenseClient {

    private static final String ML_API_URL =
        System.getenv().getOrDefault(
                "ML_API_URL",
                "http://127.0.0.1:8001/evaluate"
        );

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public MlDefenseClient() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    // =========================================================
    // EVALUATE PAYMENT
    // =========================================================

    public MlDefenseResult evaluate(PaymentRequest paymentRequest) {

        try {

            Map<String, Object> payload = new HashMap<>();

            // -------------------------------------------------
            // BASIC PAYMENT INFORMATION
            // -------------------------------------------------

            payload.put(
                    "amount",
                    paymentRequest.getAmount().doubleValue()
            );

            payload.put(
                    "merchant_category",
                    paymentRequest.getMerchantCategory().name()
            );

            payload.put(
                    "country",
                    paymentRequest.getCountry()
            );

            payload.put(
                    "payment_method",
                    paymentRequest.getPaymentMethod().name()
            );

            // -------------------------------------------------
            // TEMPORARY BEHAVIORAL VALUES
            // -------------------------------------------------
            // These will later come from real transaction
            // behavioral telemetry.

            payload.put(
                    "velocity_1h",
                    1
            );

            payload.put(
                    "device_age_days",
                    365
            );

            payload.put(
                    "account_age_days",
                    1000
            );

            // -------------------------------------------------
            // BEHAVIORAL FEATURES
            // -------------------------------------------------

            double amountDeviation =
                    Math.max(
                            0.5,
                            Math.min(
                                    paymentRequest.getAmount().doubleValue()
                                            / 2500.0,
                                    2.5
                            )
                    );

            double velocityDeviation = 1.0;

            double behavioralScore =
                    (amountDeviation + velocityDeviation) / 2.0;

            payload.put(
                    "amount_deviation_ratio",
                    amountDeviation
            );

            payload.put(
                    "velocity_deviation_ratio",
                    velocityDeviation
            );

            payload.put(
                    "behavioral_deviation_score",
                    behavioralScore
            );

            // -------------------------------------------------
            // CONVERT TO JSON
            // -------------------------------------------------

            String json =
                    objectMapper.writeValueAsString(payload);

            // -------------------------------------------------
            // CREATE HTTP REQUEST
            // -------------------------------------------------

            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(URI.create(ML_API_URL))
                            .header(
                                    "Content-Type",
                                    "application/json"
                            )
                            .POST(
                                    HttpRequest.BodyPublishers
                                            .ofString(json)
                            )
                            .build();

            // -------------------------------------------------
            // CALL PYTHON ML API
            // -------------------------------------------------

            HttpResponse<String> response =
                    httpClient.send(
                            request,
                            HttpResponse.BodyHandlers.ofString()
                    );

            // -------------------------------------------------
            // CHECK RESPONSE
            // -------------------------------------------------

            if (response.statusCode() != 200) {

                throw new RuntimeException(
                        "ML Defense API returned HTTP "
                                + response.statusCode()
                                + ": "
                                + response.body()
                );
            }

            // -------------------------------------------------
            // PARSE JSON RESPONSE
            // -------------------------------------------------

            JsonNode node =
                    objectMapper.readTree(
                            response.body()
                    );

            double riskScore =
                    node.path("risk_score")
                            .asDouble();

            double mlProbability =
                    node.path("ml_probability")
                            .asDouble();

            String riskLevel =
                    node.path("risk_level")
                            .asText();

            String decision =
                    node.path("decision")
                            .asText();

            String action =
                    node.path("action")
                            .asText();

            String reason =
                    node.path("reason")
                            .asText();

            return new MlDefenseResult(
                    riskScore,
                    mlProbability,
                    riskLevel,
                    decision,
                    action,
                    reason
            );

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            throw new RuntimeException(
                    "ML Defense API request was interrupted",
                    e
            );

        } catch (IOException e) {

            throw new RuntimeException(
                    "Unable to connect to ML Defense API at "
                            + ML_API_URL,
                    e
            );
        }
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

        public MlDefenseResult(
                double riskScore,
                double mlProbability,
                String riskLevel,
                String decision,
                String action,
                String reason) {

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