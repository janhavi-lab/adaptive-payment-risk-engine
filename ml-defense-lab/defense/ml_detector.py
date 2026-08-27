"""
Blue-Team V2 ML Detector

Loads the frozen behavioral Random Forest model and
produces fraud probability for a transaction.
"""

from pathlib import Path

import joblib
import pandas as pd


# ---------------------------------------------------------
# MODEL LOCATION
# ---------------------------------------------------------

BASE_DIR = Path(__file__).resolve().parent.parent

MODEL_PATH = (
    BASE_DIR
    / "models"
    / "fraud_detector_v2.joblib"
)


# ---------------------------------------------------------
# LOAD MODEL
# ---------------------------------------------------------

_bundle = joblib.load(MODEL_PATH)

MODEL = _bundle["model"]
PREPROCESSOR = _bundle["preprocessor"]

FEATURES = _bundle["features"]


# ---------------------------------------------------------
# DETECTOR
# ---------------------------------------------------------

def predict_fraud_probability(transaction):
    """
    Predict fraud probability for one transaction.

    Returns a value between 0 and 1.
    """

    # Create DataFrame from one transaction
    df = pd.DataFrame([transaction])

    # Make sure all expected features exist
    missing_features = [
        feature
        for feature in FEATURES
        if feature not in df.columns
    ]

    if missing_features:
        raise ValueError(
            f"Missing model features: {missing_features}"
        )

    # Keep exact model feature order
    df = df[FEATURES]

    # Apply saved preprocessing
    X = PREPROCESSOR.transform(df)

    # Fraud probability
    probabilities = MODEL.predict_proba(X)

    # Model classes are expected to be [0, 1]
    fraud_index = list(MODEL.classes_).index(1)

    fraud_probability = probabilities[
        0,
        fraud_index
    ]

    return float(fraud_probability)


# ---------------------------------------------------------
# TEST
# ---------------------------------------------------------

if __name__ == "__main__":

    test_transaction = {

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
    }

    probability = predict_fraud_probability(
        test_transaction
    )

    print()
    print("=" * 60)
    print("BLUE-TEAM V2 ML DETECTOR")
    print("=" * 60)

    print(
        f"Fraud Probability: "
        f"{probability * 100:.2f}%"
    )

    print("=" * 60)