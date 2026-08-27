import pandas as pd
from pathlib import Path

print("=" * 65)
print("BLUE TEAM V2 — ADVERSARIAL FAILURE ANALYSIS")
print("=" * 65)

# ---------------------------------------------------------
# LOAD MISSED ATTACKS
# ---------------------------------------------------------

DATA_PATH = Path("data/v2_missed_adversarial_attacks.csv")

if not DATA_PATH.exists():
    print("\nERROR: Missed attack file not found:")
    print(DATA_PATH)
    raise SystemExit(1)

df = pd.read_csv(DATA_PATH)

print(f"\nMissed attacks available: {len(df):,}")

# ---------------------------------------------------------
# ATTACK DISTRIBUTION
# ---------------------------------------------------------

print("\n" + "=" * 65)
print("MISSED ATTACK DISTRIBUTION")
print("=" * 65)

if "attack_type" in df.columns:
    distribution = df["attack_type"].value_counts()

    print(distribution)

# ---------------------------------------------------------
# ATTACK FAMILY PERCENTAGE
# ---------------------------------------------------------

print("\n" + "=" * 65)
print("MISSED ATTACK PERCENTAGE")
print("=" * 65)

if "attack_type" in df.columns:
    percentages = (
        df["attack_type"]
        .value_counts(normalize=True)
        .mul(100)
        .round(2)
    )

    for attack, percentage in percentages.items():
        print(f"{attack:<30} {percentage:>6.2f}%")

# ---------------------------------------------------------
# AVAILABLE COLUMNS
# ---------------------------------------------------------

print("\n" + "=" * 65)
print("AVAILABLE FEATURES")
print("=" * 65)

for column in df.columns:
    print(column)

# ---------------------------------------------------------
# BINARY SIGNAL ANALYSIS
# ---------------------------------------------------------

print("\n" + "=" * 65)
print("SIGNAL FREQUENCY IN MISSED ATTACKS")
print("=" * 65)

binary_columns = []

for column in df.columns:

    unique_values = df[column].dropna().unique()

    if len(unique_values) <= 2:

        try:
            numeric_values = set(
                pd.to_numeric(unique_values)
            )

            if numeric_values.issubset({0, 1}):
                binary_columns.append(column)

        except Exception:
            pass

if binary_columns:

    for column in binary_columns:

        count = int(df[column].sum())
        percentage = (count / len(df)) * 100

        print(
            f"{column:<30} "
            f"{count:>6} "
            f"({percentage:>6.2f}%)"
        )

else:
    print("No binary signal columns detected.")

# ---------------------------------------------------------
# BEHAVIORAL FEATURE ANALYSIS
# ---------------------------------------------------------

print("\n" + "=" * 65)
print("BEHAVIORAL FEATURE SUMMARY")
print("=" * 65)

behavioral_keywords = [
    "amount",
    "velocity",
    "behavior",
    "deviation",
    "device",
    "account"
]

behavioral_columns = []

for column in df.columns:

    column_lower = column.lower()

    if any(
        keyword in column_lower
        for keyword in behavioral_keywords
    ):
        if pd.api.types.is_numeric_dtype(df[column]):
            behavioral_columns.append(column)

if behavioral_columns:

    print(
        df[behavioral_columns]
        .describe()
        .round(2)
    )

else:
    print("No behavioral numeric features detected.")

# ---------------------------------------------------------
# ATTACK FAMILY FEATURE SUMMARY
# ---------------------------------------------------------

if "attack_type" in df.columns:

    print("\n" + "=" * 65)
    print("FEATURE SUMMARY BY ATTACK FAMILY")
    print("=" * 65)

    for attack_type in df["attack_type"].unique():

        attack_df = df[
            df["attack_type"] == attack_type
        ]

        print("\n" + "-" * 65)
        print(f"ATTACK: {attack_type}")
        print(f"Missed: {len(attack_df):,}")

        if behavioral_columns:

            print(
                attack_df[behavioral_columns]
                .describe()
                .loc[["mean", "min", "50%", "max"]]
                .round(2)
            )

# ---------------------------------------------------------
# SAMPLE MISSED ATTACKS
# ---------------------------------------------------------

print("\n" + "=" * 65)
print("SAMPLE MISSED ATTACKS")
print("=" * 65)

print(
    df.head(20).to_string(index=False)
)

# ---------------------------------------------------------
# SAVE CLEAN ANALYSIS
# ---------------------------------------------------------

output_path = Path(
    "data/v2_failure_analysis.csv"
)

if "attack_type" in df.columns:

    summary = (
        df["attack_type"]
        .value_counts()
        .reset_index()
    )

    summary.columns = [
        "attack_type",
        "missed_attacks"
    ]

    summary["percentage"] = (
        summary["missed_attacks"]
        / len(df)
        * 100
    ).round(2)

    summary.to_csv(
        output_path,
        index=False
    )

    print(
        f"\nSummary saved to:\n{output_path}"
    )

print("\n" + "=" * 65)
print("V2 FAILURE ANALYSIS COMPLETE")
print("=" * 65)