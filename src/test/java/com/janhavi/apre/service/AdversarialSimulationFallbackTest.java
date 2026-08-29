package com.janhavi.apre.service;

import com.janhavi.apre.rules.RiskRule;
import com.janhavi.apre.rules.impl.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class AdversarialSimulationFallbackTest {

    private AdversarialSimulationFallback fallback;

    @BeforeEach
    public void setUp() {
        List<RiskRule> rules = List.of(
                new AmountRule(),
                new CountryRule(),
                new MerchantRule(),
                new PaymentMethodRule(),
                new TimeRule(),
                new VelocityRule()
        );
        fallback = new AdversarialSimulationFallback(rules);
    }

    @Test
    public void testAutoSelectWith25Attacks() {
        Map<String, Object> result = fallback.simulateAttack("AUTO", 25);
        assertNotNull(result);
        assertEquals(25, result.get("transactions_generated"));
        assertNotNull(result.get("test_id"));
        assertNotNull(result.get("attack_type"));
        assertNotNull(result.get("attack_name"));
        assertNotNull(result.get("detection_rate"));
        assertNotNull(result.get("defense_status"));
        assertNotNull(result.get("weakness_identified"));
        assertNotNull(result.get("feedback_candidate_status"));
        assertNotNull(result.get("adaptive_insight"));
        assertNotNull(result.get("next_defense_focus"));
        assertEquals("ORCHESTRATOR_FALLBACK", result.get("simulation_source"));

        int detected = (int) result.get("detected_count");
        int missed = (int) result.get("missed_count");
        assertEquals(25, detected + missed);
        assertTrue((double) result.get("detection_rate") >= 0.0 && (double) result.get("detection_rate") <= 100.0);
    }

    @Test
    public void testAutoSelectWith50Attacks() {
        Map<String, Object> result = fallback.simulateAttack("AUTO", 50);
        assertNotNull(result);
        assertEquals(50, result.get("transactions_generated"));
        int detected = (int) result.get("detected_count");
        int missed = (int) result.get("missed_count");
        assertEquals(50, detected + missed);
    }

    @Test
    public void testExplicitVelocityAbuseScenario() {
        Map<String, Object> result = fallback.simulateAttack("VELOCITY_ABUSE", 50);
        assertNotNull(result);
        assertEquals("VELOCITY_ABUSE", result.get("attack_type"));
        assertEquals(50, result.get("transactions_generated"));
        assertEquals("VELOCITY_ABUSE", result.get("next_defense_focus"));
    }

    @Test
    public void testExplicitAccountTakeoverScenario() {
        Map<String, Object> result = fallback.simulateAttack("ACCOUNT_TAKEOVER", 50);
        assertNotNull(result);
        assertEquals("ACCOUNT_TAKEOVER", result.get("attack_type"));
        assertEquals(50, result.get("transactions_generated"));
    }

    @Test
    public void test100AttacksBatch() {
        Map<String, Object> result = fallback.simulateAttack("MULTI_SIGNAL_ATTACK", 100);
        assertNotNull(result);
        assertEquals(100, result.get("transactions_generated"));
        int detected = (int) result.get("detected_count");
        int missed = (int) result.get("missed_count");
        assertEquals(100, detected + missed);
    }

    @Test
    public void testSampleTransactionsStructure() {
        Map<String, Object> result = fallback.simulateAttack("GEOGRAPHIC_ANOMALY", 50);
        List<?> sampleTxs = (List<?>) result.get("sample_transactions");
        assertNotNull(sampleTxs);
        assertFalse(sampleTxs.isEmpty());
        assertTrue(sampleTxs.size() <= 10);

        Map<?, ?> firstTx = (Map<?, ?>) sampleTxs.get(0);
        assertNotNull(firstTx.get("id"));
        assertNotNull(firstTx.get("amount"));
        assertNotNull(firstTx.get("country"));
        assertNotNull(firstTx.get("payment_method"));
        assertNotNull(firstTx.get("risk_score"));
        assertNotNull(firstTx.get("risk_level"));
        assertNotNull(firstTx.get("decision"));
        assertNotNull(firstTx.get("detected"));
    }
}
