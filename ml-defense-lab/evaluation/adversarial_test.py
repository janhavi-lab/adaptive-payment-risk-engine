"""
Adversarial Robustness Evaluation

Tests the previously trained Blue-Team detector against
novel adversarial transactions that were NOT used during
training.

The model remains frozen during this evaluation.
"""

from pathlib import Path

import joblib
import pandas as pd

from sklearn.metrics import (
    precision_score,
    recall_score,
    f1_score,
    confusion_matrix,
)


# ============================================================
# PATHS
# ============================================================

BASE_DIR = Path(__file__).resolve().parent.parent

MODEL_FILE = (
    BASE_DIR
    / "models"
    / "fraud_detector.joblib"
)

ATTACK_FILE = (
    BASE_DIR
    / "data"
    / "adversarial_attacks.csv"
)


# ============================================================
# LOAD MODEL
# ============================================================

print()
print("=" * 65)
print("ADAPTIVE ADVERSARIAL PAYMENT DEFENSE LAB")
print("RED TEAM → BLUE TEAM ROBUSTNESS TEST")
print("=" * 65)
print()

print("Loading frozen Blue-Team detector...")

model = joblib.load(
    MODEL_FILE
)

print("Model loaded.")
print()


# ============================================================
# LOAD ADVERSARIAL DATA
# ============================================================

df = pd.read_csv(
    ATTACK_FILE
)

print(
    f"Adversarial transactions: {len(df):,}"
)

print()


# ============================================================
# FEATURES
# ============================================================

FEATURES = [
    "amount",
    "merchant_category",
    "country",
    "payment_method",
    "velocity_1h",
    "device_age_days",
    "location_change",
    "new_payment_method",
    "account_age_days",
    "merchant_anomaly",
    "amount_anomaly",
]

TARGET = "is_fraud"


X = df[FEATURES]

y_true = df[TARGET]


# ============================================================
# PREDICTION
# ============================================================

print("Running adversarial attack simulation...")

y_pred = model.predict(
    X
)


# ============================================================
# METRICS
# ============================================================

precision = precision_score(
    y_true,
    y_pred,
    zero_division=0,
)

recall = recall_score(
    y_true,
    y_pred,
    zero_division=0,
)

f1 = f1_score(
    y_true,
    y_pred,
    zero_division=0,
)


# ============================================================
# CONFUSION MATRIX
# ============================================================

tn, fp, fn, tp = confusion_matrix(
    y_true,
    y_pred,
    labels=[0, 1],
).ravel()


# ============================================================
# MISSED ATTACKS
# ============================================================

missed_attacks = (
    df[
        (y_true == 1)
        & (y_pred == 0)
    ]
    .copy()
)


missed_count = len(
    missed_attacks
)


miss_rate = (
    missed_count / len(df)
    if len(df) > 0
    else 0
)


# ============================================================
# RESULTS
# ============================================================

print()
print("=" * 65)
print("ADVERSARIAL ROBUSTNESS RESULTS")
print("=" * 65)

print()

print(
    f"Precision:          {precision * 100:.2f}%"
)

print(
    f"Recall:             {recall * 100:.2f}%"
)

print(
    f"F1 Score:           {f1 * 100:.2f}%"
)

print(
    f"Missed Attacks:     {missed_count:,}"
)

print(
    f"Attack Miss Rate:   {miss_rate * 100:.2f}%"
)

print()

print("Confusion Matrix:")

print()

print(
    confusion_matrix(
        y_true,
        y_pred,
        labels=[0, 1],
    )
)

print()


# ============================================================
# ATTACK-TYPE ANALYSIS
# ============================================================

print("=" * 65)
print("ATTACK FAMILY DETECTION")
print("=" * 65)

print()

attack_results = []

for attack_type in sorted(
    df["attack_type"].unique()
):

    attack_df = df[
        df["attack_type"] == attack_type
    ]

    attack_predictions = model.predict(
        attack_df[FEATURES]
    )

    detected = (
        attack_predictions == 1
    ).sum()

    total = len(
        attack_df
    )

    recall = (
        detected / total
        if total > 0
        else 0
    )

    attack_results.append({

        "attack_type":
            attack_type,

        "total":
            total,

        "detected":
            int(detected),

        "missed":
            int(total - detected),

        "recall":
            round(
                recall * 100,
                2
            )
    })


attack_results_df = pd.DataFrame(
    attack_results
)

print(
    attack_results_df.to_string(
        index=False
    )
)

print()


# ============================================================
# SAVE MISSED ATTACKS
# ============================================================

MISSED_FILE = (
    BASE_DIR
    / "data"
    / "missed_adversarial_attacks.csv"
)

missed_attacks.to_csv(
    MISSED_FILE,
    index=False
)

print(
    f"Missed attacks saved to:"
)

print(
    MISSED_FILE
)

print()

print("=" * 65)
print("ROBUSTNESS TEST COMPLETE")
print("=" * 65)
print()