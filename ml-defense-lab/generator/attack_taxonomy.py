"""
Attack Taxonomy for the Adaptive Adversarial Payment Defense Lab.

This module defines defensive simulation scenarios.
It does NOT perform real-world fraud or unauthorized payment activity.
"""

ATTACK_TAXONOMY = {

    "ACCOUNT_TAKEOVER": {
        "name": "Account Takeover",
        "description": (
            "Simulates a transaction inconsistent with the customer's "
            "usual device, location, velocity, or spending behavior."
        ),
        "signals": [
            "new_device",
            "location_change",
            "unusual_amount",
            "unusual_payment_method",
            "high_velocity"
        ]
    },

    "SYNTHETIC_IDENTITY": {
        "name": "Synthetic Identity",
        "description": (
            "Simulates an account with limited behavioral history "
            "showing suspicious transaction characteristics."
        ),
        "signals": [
            "young_account",
            "limited_history",
            "unusual_amount",
            "unusual_merchant",
            "unusual_payment_method"
        ]
    },

    "TRANSACTION_ANOMALY": {
        "name": "Transaction Anomaly",
        "description": (
            "Simulates a transaction whose amount or merchant behavior "
            "deviates significantly from the customer's normal pattern."
        ),
        "signals": [
            "unusual_amount",
            "unusual_merchant_category"
        ]
    },

    "VELOCITY_ABUSE": {
        "name": "Velocity Abuse",
        "description": (
            "Simulates an unusually high number of transactions "
            "within a short period."
        ),
        "signals": [
            "high_velocity",
            "repeated_merchant",
            "rapid_payment_activity"
        ]
    },

    "GEOGRAPHIC_ANOMALY": {
        "name": "Geographic Anomaly",
        "description": (
            "Simulates payment activity occurring from a location "
            "that differs from the customer's established behavior."
        ),
        "signals": [
            "location_change",
            "country_change",
            "travel_impossibility"
        ]
    },

    "MERCHANT_ANOMALY": {
        "name": "Merchant Anomaly",
        "description": (
            "Simulates activity involving a merchant category "
            "that is unusual for the customer's historical behavior."
        ),
        "signals": [
            "unusual_merchant",
            "unusual_merchant_category"
        ]
    },

    "PAYMENT_METHOD_ABUSE": {
        "name": "Payment Method Abuse",
        "description": (
            "Simulates a transaction using a payment method "
            "that differs from the customer's normal pattern."
        ),
        "signals": [
            "new_payment_method",
            "unusual_payment_method"
        ]
    },

    "MULTI_SIGNAL_ATTACK": {
        "name": "Multi-Signal Attack",
        "description": (
            "Combines multiple suspicious behavioral signals "
            "to simulate a more difficult adversarial scenario."
        ),
        "signals": [
            "unusual_amount",
            "location_change",
            "new_device",
            "high_velocity",
            "unusual_payment_method",
            "unusual_merchant"
        ]
    }
}


def get_attack_types():
    """Return all supported attack type identifiers."""
    return list(ATTACK_TAXONOMY.keys())


def get_attack_profile(attack_type):
    """Return the profile for a specific attack type."""
    return ATTACK_TAXONOMY.get(attack_type)


if __name__ == "__main__":

    print("\n=== ADAPTIVE ADVERSARIAL PAYMENT DEFENSE LAB ===")
    print("Attack Taxonomy\n")

    for attack_type, profile in ATTACK_TAXONOMY.items():

        print(f"{attack_type}")
        print(f"  Name: {profile['name']}")
        print(f"  Signals: {', '.join(profile['signals'])}")
        print()