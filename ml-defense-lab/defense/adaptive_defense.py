"""
Adaptive Adversarial Payment Defense

Blue Team Layer

Combines:
1. Blue-Team V2 ML fraud detection
2. Behavioral signals
3. Transaction signals
4. Adaptive risk scoring
5. Explainable mitigation decisions

Pipeline:

Payment Transaction
        ↓
V2 ML Detector
        ↓
Fraud Probability
        ↓
Adaptive Risk Engine
        ↓
Risk Level
        ↓
Mitigation Decision
        ↓
ALLOW / STEP_UP / BLOCK
"""

from defense.ml_detector import predict_fraud_probability

from defense.risk_engine import (
    calculate_risk_score,
    classify_risk,
)

from defense.mitigation import (
    recommend_action,
)


# =========================================================
# PAYMENT METHOD RISK
# =========================================================

def calculate_payment_method_risk(payment_method):
    """
    Assign a simple risk contribution to the payment method.

    This is NOT the ML model prediction.
    It is an additional rule-based signal used by
    the adaptive risk engine.
    """

    payment_method = str(
        payment_method
    ).upper()

    risk_map = {
        "CREDIT_CARD": 0.30,
        "DEBIT_CARD": 0.35,
        "UPI": 0.40,
        "NET_BANKING": 0.45,
        "WALLET": 0.50,
    }

    return risk_map.get(
        payment_method,
        0.50
    )


# =========================================================
# TRANSACTION ANALYSIS
# =========================================================

def analyze_transaction(transaction):
    """
    Analyze one payment transaction after ML prediction.

    Expected fields:

    ml_probability
    behavioral_deviation_score
    velocity_1h
    payment_method_risk
    merchant_anomaly
    location_change
    new_payment_method
    device_age_days
    """

    # -----------------------------------------------------
    # ML PROBABILITY
    # -----------------------------------------------------

    ml_probability = float(
        transaction.get(
            "ml_probability",
            0
        )
    )

    # -----------------------------------------------------
    # BEHAVIORAL SCORE
    # -----------------------------------------------------

    behavioral_score = float(
        transaction.get(
            "behavioral_deviation_score",
            0
        )
    )

    # -----------------------------------------------------
    # VELOCITY
    # -----------------------------------------------------

    velocity = int(
        transaction.get(
            "velocity_1h",
            0
        )
    )

    # -----------------------------------------------------
    # PAYMENT METHOD RISK
    # -----------------------------------------------------

    payment_method_risk = float(
        transaction.get(
            "payment_method_risk",
            0
        )
    )

    # -----------------------------------------------------
    # OTHER SECURITY SIGNALS
    # -----------------------------------------------------

    merchant_anomaly = int(
        transaction.get(
            "merchant_anomaly",
            0
        )
    )

    location_change = int(
        transaction.get(
            "location_change",
            0
        )
    )

    new_payment_method = int(
        transaction.get(
            "new_payment_method",
            0
        )
    )

    device_age_days = int(
        transaction.get(
            "device_age_days",
            999
        )
    )

    # =====================================================
    # CALCULATE FINAL ADAPTIVE RISK SCORE
    # =====================================================

    risk_score = calculate_risk_score(
        ml_probability=ml_probability,
        behavioral_score=behavioral_score,
        velocity_1h=velocity,
        payment_method_risk=payment_method_risk,
        merchant_anomaly=merchant_anomaly,
        location_change=location_change,
        new_payment_method=new_payment_method,
        device_age_days=device_age_days,
    )

    # -----------------------------------------------------
    # CLASSIFY RISK
    # -----------------------------------------------------

    risk_level = classify_risk(
        risk_score
    )

    # -----------------------------------------------------
    # RECOMMEND MITIGATION
    # -----------------------------------------------------

    mitigation = recommend_action(
        risk_score=risk_score,
        ml_probability=ml_probability,
    )

    # =====================================================
    # EXPLAINABLE SECURITY SIGNALS
    # =====================================================

    signals = []

    if ml_probability >= 0.70:
        signals.append(
            "High ML fraud probability"
        )

    if behavioral_score >= 1.5:
        signals.append(
            "Unusual behavioral deviation"
        )

    if velocity >= 5:
        signals.append(
            "High transaction velocity"
        )

    if merchant_anomaly:
        signals.append(
            "Merchant anomaly detected"
        )

    if location_change:
        signals.append(
            "Location change detected"
        )

    if new_payment_method:
        signals.append(
            "New payment method detected"
        )

    if device_age_days < 30:
        signals.append(
            "New device detected"
        )

    # -----------------------------------------------------
    # DEFAULT EXPLANATION
    # -----------------------------------------------------

    if not signals:
        signals.append(
            "No major fraud signals detected"
        )

    # =====================================================
    # FINAL RESULT
    # =====================================================

    return {
        "risk_score": round(
            float(risk_score),
            2
        ),

        "risk_level": risk_level,

        "decision": mitigation[
            "decision"
        ],

        "action": mitigation[
            "action"
        ],

        "reason": mitigation[
            "reason"
        ],

        "signals": signals,

        "ml_probability": round(
            ml_probability,
            4
        ),
    }


# =========================================================
# MAIN PAYMENT EVALUATION PIPELINE
# =========================================================

def evaluate_payment(payment):
    """
    Complete Blue-Team payment evaluation pipeline.

    Flow:

        Raw Payment
             ↓
        V2 ML Detector
             ↓
        Fraud Probability
             ↓
        Adaptive Risk Engine
             ↓
        Final Decision
    """

    # -----------------------------------------------------
    # COPY INPUT
    # -----------------------------------------------------

    transaction = dict(
        payment
    )

    # =====================================================
    # REQUIRED V2 ML FEATURES
    # =====================================================

    required_features = [
        "amount",
        "velocity_1h",
        "device_age_days",
        "account_age_days",
        "amount_deviation_ratio",
        "velocity_deviation_ratio",
        "behavioral_deviation_score",
        "merchant_category",
        "country",
        "payment_method",
    ]

    # -----------------------------------------------------
    # VALIDATE INPUT
    # -----------------------------------------------------

    missing_features = [
        feature
        for feature in required_features
        if feature not in transaction
    ]

    if missing_features:
        raise ValueError(
            f"Missing payment features: "
            f"{missing_features}"
        )

    # =====================================================
    # BLUE-TEAM V2 ML DETECTOR
    # =====================================================

    fraud_probability = (
        predict_fraud_probability(
            transaction
        )
    )

    # Add ML result to transaction
    transaction[
        "ml_probability"
    ] = fraud_probability

    # =====================================================
    # PAYMENT METHOD RISK
    # =====================================================

    transaction[
        "payment_method_risk"
    ] = calculate_payment_method_risk(
        transaction[
            "payment_method"
        ]
    )

    # =====================================================
    # OPTIONAL REAL-TIME SIGNALS
    # =====================================================

    # These signals may come from:
    #
    # - user history
    # - device intelligence
    # - merchant intelligence
    # - geolocation
    # - payment history
    #
    # For the current API demo, they default to 0
    # unless supplied by the caller.

    transaction.setdefault(
        "merchant_anomaly",
        0
    )

    transaction.setdefault(
        "location_change",
        0
    )

    transaction.setdefault(
        "new_payment_method",
        0
    )

    # =====================================================
    # ADAPTIVE RISK ANALYSIS
    # =====================================================

    result = analyze_transaction(
        transaction
    )

    # =====================================================
    # ADD TRANSACTION INFORMATION
    # =====================================================

    result[
        "amount"
    ] = transaction["amount"]

    result[
        "payment_method"
    ] = transaction[
        "payment_method"
    ]

    result[
        "merchant_category"
    ] = transaction[
        "merchant_category"
    ]

    result[
        "country"
    ] = transaction[
        "country"
    ]

    # =====================================================
    # RETURN FINAL DEFENSE RESULT
    # =====================================================

    return result


# =========================================================
# LOCAL TEST
# =========================================================

if __name__ == "__main__":

    demo_payment = {

        "amount": 4500,

        "velocity_1h": 6,

        "device_age_days": 5,

        "account_age_days": 1200,

        "amount_deviation_ratio": 1.8,

        "velocity_deviation_ratio": 3.0,

        "behavioral_deviation_score": 2.4,

        "merchant_category": "ECOMMERCE",

        "country": "IN",

        "payment_method": "UPI",

        "merchant_anomaly": 1,

        "location_change": 1,

        "new_payment_method": 1,
    }

    result = evaluate_payment(
        demo_payment
    )

    print()
    print("=" * 60)
    print("ADAPTIVE ADVERSARIAL PAYMENT DEFENSE")
    print("=" * 60)

    for key, value in result.items():

        print(
            f"{key}: {value}"
        )

    print("=" * 60)