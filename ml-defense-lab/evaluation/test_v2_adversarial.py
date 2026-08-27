"""
BLUE TEAM V2
Adversarial Robustness Evaluation

Tests the behavioral detector against the SAME
2,000 adversarial transactions used to evaluate V1.
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
    / "fraud_detector_v2.joblib"
)

ATTACK_FILE = (
    BASE_DIR
    / "data"
    / "behavioral_adversarial_attacks.csv"
)


# ============================================================
# LOAD MODEL
# ============================================================

print()
print("=" * 65)
print("ADAPTIVE ADVERSARIAL PAYMENT DEFENSE LAB")
print("BLUE TEAM V2 → ADVERSARIAL ROBUSTNESS TEST")
print("=" * 65)
print()

print("Loading Blue-Team V2 detector...")

artifact = joblib.load(
    MODEL_FILE
)

model = artifact["model"]
preprocessor = artifact["preprocessor"]
features = artifact["features"]

print("Model loaded.")
print()


# ============================================================
# LOAD ATTACK DATA
# ============================================================

df = pd.read_csv(
    ATTACK_FILE
)

print(
    f"Adversarial transactions: {len(df):,}"
)

print()


# ============================================================
# FEATURE PREPARATION
# ============================================================

X = df[features]

y_true = df["is_fraud"]


print(
    "Preparing adversarial features..."
)

X_processed = (
    preprocessor.transform(
        X
    )
)


# ============================================================
# PREDICTION
# ============================================================

print(
    "Running V2 against adversarial attacks..."
)

y_pred = model.predict(
    X_processed
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


missed = fn

miss_rate = (
    missed / len(df)
    if len(df) > 0
    else 0
)


# ============================================================
# RESULTS
# ============================================================

print()
print("=" * 65)
print("BLUE-TEAM V2 ADVERSARIAL RESULTS")
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
    f"Missed Attacks:     {missed:,}"
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
# ATTACK FAMILY ANALYSIS
# ============================================================

print("=" * 65)
print("V2 ATTACK FAMILY DETECTION")
print("=" * 65)
print()

results = []

for attack_type in sorted(
    df["attack_type"].unique()
):

    attack_df = df[
        df["attack_type"] == attack_type
    ]

    attack_X = (
        attack_df[features]
    )

    attack_X_processed = (
        preprocessor.transform(
            attack_X
        )
    )

    attack_predictions = (
        model.predict(
            attack_X_processed
        )
    )

    total = len(
        attack_df
    )

    detected = (
        attack_predictions == 1
    ).sum()

    missed_attack = (
        total - detected
    )

    attack_recall = (
        detected / total
        if total > 0
        else 0
    )

    results.append({

        "attack_type":
            attack_type,

        "total":
            total,

        "detected":
            int(detected),

        "missed":
            int(missed_attack),

        "recall":
            round(
                attack_recall * 100,
                2
            )
    })


results_df = pd.DataFrame(
    results
)

print(
    results_df.to_string(
        index=False
    )
)

print()


# ============================================================
# SAVE MISSED ATTACKS
# ============================================================

missed_df = df[
    (y_true == 1)
    & (y_pred == 0)
].copy()


output_file = (
    BASE_DIR
    / "data"
    / "v2_missed_adversarial_attacks.csv"
)

missed_df.to_csv(
    output_file,
    index=False
)


print(
    f"V2 missed attacks saved to:"
)

print(
    output_file
)

print()

print("=" * 65)
print("V2 ADVERSARIAL TEST COMPLETE")
print("=" * 65)
print()