"""
BLUE TEAM V2
Behavioral Fraud Detector

Trains a fraud detector using transaction-level and
customer-behavioral features.
"""

from pathlib import Path

import joblib
import pandas as pd

from sklearn.compose import ColumnTransformer
from sklearn.ensemble import RandomForestClassifier
from sklearn.metrics import (
    classification_report,
    confusion_matrix,
    precision_score,
    recall_score,
    f1_score,
)
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import OneHotEncoder


# ============================================================
# PATHS
# ============================================================

BASE_DIR = Path(__file__).resolve().parent.parent

DATA_FILE = (
    BASE_DIR
    / "data"
    / "behavioral_payments.csv"
)

MODEL_DIR = (
    BASE_DIR
    / "models"
)

MODEL_DIR.mkdir(
    exist_ok=True
)

MODEL_FILE = (
    MODEL_DIR
    / "fraud_detector_v2.joblib"
)


# ============================================================
# LOAD DATA
# ============================================================

print()
print("=" * 65)
print("ADAPTIVE ADVERSARIAL PAYMENT DEFENSE LAB")
print("BLUE TEAM V2 — BEHAVIORAL DETECTOR")
print("=" * 65)
print()

print(
    "Loading behavioral dataset..."
)

df = pd.read_csv(
    DATA_FILE
)

print(
    f"Dataset loaded: {len(df):,} transactions"
)

print()


# ============================================================
# FEATURES
# ============================================================

NUMERIC_FEATURES = [
    "amount",
    "velocity_1h",
    "device_age_days",
    "account_age_days",
    "amount_deviation_ratio",
    "velocity_deviation_ratio",
    "behavioral_deviation_score",
]

CATEGORICAL_FEATURES = [
    "merchant_category",
    "country",
    "payment_method",
]

FEATURES = (
    NUMERIC_FEATURES
    + CATEGORICAL_FEATURES
)

TARGET = "is_fraud"


X = df[FEATURES]

y = df[TARGET]


# ============================================================
# TRAIN / TEST SPLIT
# ============================================================

X_train, X_test, y_train, y_test = train_test_split(
    X,
    y,
    test_size=0.20,
    random_state=42,
    stratify=y,
)


print(
    f"Training samples: {len(X_train):,}"
)

print(
    f"Testing samples:  {len(X_test):,}"
)

print()


# ============================================================
# PREPROCESSING
# ============================================================

preprocessor = ColumnTransformer(

    transformers=[

        (
            "categorical",

            OneHotEncoder(
                handle_unknown="ignore"
            ),

            CATEGORICAL_FEATURES,
        ),

        (
            "numeric",

            "passthrough",

            NUMERIC_FEATURES,
        ),
    ]
)


# ============================================================
# MODEL
# ============================================================

model = RandomForestClassifier(

    n_estimators=300,

    max_depth=14,

    min_samples_leaf=2,

    class_weight="balanced",

    random_state=42,

    n_jobs=-1,
)


# ============================================================
# TRANSFORM DATA
# ============================================================

print(
    "Preparing behavioral features..."
)

X_train_processed = (
    preprocessor.fit_transform(
        X_train
    )
)

X_test_processed = (
    preprocessor.transform(
        X_test
    )
)


# ============================================================
# TRAIN
# ============================================================

print(
    "Training Blue-Team V2 detector..."
)

model.fit(
    X_train_processed,
    y_train,
)


# ============================================================
# PREDICTION
# ============================================================

y_pred = model.predict(
    X_test_processed
)


# ============================================================
# METRICS
# ============================================================

precision = precision_score(
    y_test,
    y_pred,
    zero_division=0,
)

recall = recall_score(
    y_test,
    y_pred,
    zero_division=0,
)

f1 = f1_score(
    y_test,
    y_pred,
    zero_division=0,
)


# ============================================================
# CONFUSION MATRIX
# ============================================================

tn, fp, fn, tp = confusion_matrix(
    y_test,
    y_pred,
    labels=[0, 1],
).ravel()


false_positive_rate = (
    fp / (fp + tn)
    if (fp + tn) > 0
    else 0
)


# ============================================================
# RESULTS
# ============================================================

print()
print("=" * 65)
print("BLUE-TEAM V2 RESULTS")
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
    f"False Positive Rate:{false_positive_rate * 100:.2f}%"
)

print()

print("Confusion Matrix:")

print()

print(
    confusion_matrix(
        y_test,
        y_pred,
        labels=[0, 1],
    )
)

print()

print("Classification Report:")

print()

print(
    classification_report(
        y_test,
        y_pred,
        target_names=[
            "Legitimate",
            "Fraud",
        ],
        zero_division=0,
    )
)


# ============================================================
# SAVE MODEL + PREPROCESSOR
# ============================================================

artifact = {

    "model":
        model,

    "preprocessor":
        preprocessor,

    "features":
        FEATURES,

    "numeric_features":
        NUMERIC_FEATURES,

    "categorical_features":
        CATEGORICAL_FEATURES,
}


joblib.dump(
    artifact,
    MODEL_FILE,
)


print()
print("=" * 65)
print("MODEL SAVED")
print("=" * 65)
print()

print(
    f"Model: {MODEL_FILE}"
)

print()