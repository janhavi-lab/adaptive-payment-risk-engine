"""
Adaptive mitigation layer.

Converts risk level into an appropriate payment action.
"""


def recommend_action(
    risk_score,
    attack_type=None,
    ml_probability=0.0,
):
    """
    Recommend an action based on risk.
    """

    # Critical attacks should be blocked immediately.
    if risk_score >= 80:
        return {
            "decision": "BLOCK",
            "action": "BLOCK_TRANSACTION",
            "reason": "Critical fraud risk detected"
        }

    # High risk requires additional verification.
    if risk_score >= 60:
        return {
            "decision": "STEP_UP",
            "action": "REQUIRE_ADDITIONAL_AUTHENTICATION",
            "reason": "High fraud risk detected"
        }

    # Medium risk gets monitoring.
    if risk_score >= 35:
        return {
            "decision": "MONITOR",
            "action": "APPROVE_WITH_MONITORING",
            "reason": "Moderate behavioral risk detected"
        }

    return {
        "decision": "APPROVE",
        "action": "APPROVE_TRANSACTION",
        "reason": "Low risk transaction"
    }