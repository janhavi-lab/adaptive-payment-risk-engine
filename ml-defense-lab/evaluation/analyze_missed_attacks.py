"""
Analyze attacks missed by the frozen Blue-Team detector.
"""

from pathlib import Path

import pandas as pd


BASE_DIR = Path(__file__).resolve().parent.parent

MISSED_FILE = (
    BASE_DIR
    / "data"
    / "missed_adversarial_attacks.csv"
)


df = pd.read_csv(
    MISSED_FILE
)


print()
print("=" * 65)
print("ADVERSARIAL FAILURE ANALYSIS")
print("=" * 65)
print()

print(
    f"Missed attacks available: {len(df):,}"
)

print()


# ============================================================
# ATTACK TYPES
# ============================================================

print("Attack types:")
print()

print(
    df["attack_type"]
    .value_counts()
    .to_string()
)

print()


# ============================================================
# SIGNAL FREQUENCY
# ============================================================

signals = [
    "location_change",
    "new_payment_method",
    "merchant_anomaly",
    "amount_anomaly",
]


print("=" * 65)
print("SIGNAL FREQUENCY IN MISSED ATTACKS")
print("=" * 65)
print()


for signal in signals:

    count = (
        df[signal] == 1
    ).sum()

    percentage = (
        count / len(df) * 100
        if len(df) > 0
        else 0
    )

    print(
        f"{signal:25} "
        f"{count:4} "
        f"({percentage:6.2f}%)"
    )


print()


# ============================================================
# NUMERIC BEHAVIOR
# ============================================================

print("=" * 65)
print("MISSED ATTACK BEHAVIOR")
print("=" * 65)
print()


numeric_columns = [
    "amount",
    "velocity_1h",
    "device_age_days",
    "account_age_days",
]


print(
    df[numeric_columns]
    .describe()
    .round(2)
    .to_string()
)

print()


# ============================================================
# SAMPLE
# ============================================================

print("=" * 65)
print("SAMPLE MISSED ATTACKS")
print("=" * 65)
print()

print(
    df.head(15).to_string(
        index=False
    )
)

print()

print("=" * 65)
print("FAILURE ANALYSIS COMPLETE")
print("=" * 65)
print()
