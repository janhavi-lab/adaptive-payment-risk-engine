"""
Adaptive Risk Engine

Combines:
1. ML fraud probability
2. Behavioral deviation
3. Transaction velocity
4. Payment method risk
5. Merchant anomaly
6. Account/device signals

Produces an explainable final risk score.
"""


def calculate_risk_score(
    ml_probability,
    behavioral_score,
    velocity_1h,
    payment_method_risk,
    merchant_anomaly,
    location_change,
    new_payment_method,
    device_age_days,
):
    """
    Calculate final adaptive risk score.

    All inputs are normalized into a 0-100 risk score.
    """

    score = 0.0

    # --------------------------------------------------
    # 1. ML MODEL
    # --------------------------------------------------

    score += ml_probability * 45

    # --------------------------------------------------
    # 2. BEHAVIORAL ANOMALY
    # --------------------------------------------------

    behavioral_component = min(behavioral_score / 3.0, 1.0)

    score += behavioral_component * 20

    # --------------------------------------------------
    # 3. VELOCITY
    # --------------------------------------------------

    if velocity_1h >= 7:
        score += 12
    elif velocity_1h >= 5:
        score += 8
    elif velocity_1h >= 3:
        score += 4

    # --------------------------------------------------
    # 4. PAYMENT METHOD
    # --------------------------------------------------

    score += payment_method_risk * 8

    # --------------------------------------------------
    # 5. MERCHANT ANOMALY
    # --------------------------------------------------

    if merchant_anomaly:
        score += 7

    # --------------------------------------------------
    # 6. LOCATION CHANGE
    # --------------------------------------------------

    if location_change:
        score += 5

    # --------------------------------------------------
    # 7. NEW PAYMENT METHOD
    # --------------------------------------------------

    if new_payment_method:
        score += 5

    # --------------------------------------------------
    # 8. DEVICE AGE
    # --------------------------------------------------

    if device_age_days < 7:
        score += 5
    elif device_age_days < 30:
        score += 3

    return round(min(score, 100), 2)


def classify_risk(score):

    if score >= 80:
        return "CRITICAL"

    if score >= 60:
        return "HIGH"

    if score >= 35:
        return "MEDIUM"

    return "LOW"