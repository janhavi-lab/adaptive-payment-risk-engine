def make_decision(risk_score):

    if risk_score >= 80:
        return {
            "risk_level": "CRITICAL",
            "decision": "BLOCK",
            "action": "BLOCK_TRANSACTION"
        }

    elif risk_score >= 60:
        return {
            "risk_level": "HIGH",
            "decision": "REVIEW",
            "action": "MANUAL_REVIEW"
        }

    elif risk_score >= 30:
        return {
            "risk_level": "MEDIUM",
            "decision": "STEP_UP",
            "action": "ADDITIONAL_VERIFICATION"
        }

    else:
        return {
            "risk_level": "LOW",
            "decision": "ALLOW",
            "action": "ALLOW_TRANSACTION"
        }