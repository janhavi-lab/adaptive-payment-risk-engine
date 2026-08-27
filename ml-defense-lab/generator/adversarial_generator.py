"""
Adversarial Attack Mutation Layer

Creates harder synthetic payment scenarios by mutating
legitimate-looking transactions with subtle behavioral
inconsistencies.

This is a defensive simulation environment for fraud detection.
"""

import random
from pathlib import Path

import pandas as pd


random.seed(123)


BASE_DIR = Path(__file__).resolve().parent.parent

INPUT_FILE = (
    BASE_DIR
    / "data"
    / "synthetic_payments.csv"
)

OUTPUT_FILE = (
    BASE_DIR
    / "data"
    / "adversarial_attacks.csv"
)


def create_subtle_amount_anomaly(tx):

    tx = tx.copy()

    # Moderate deviation instead of extreme deviation
    multiplier = random.uniform(
        1.5,
        2.5
    )

    tx["amount"] = round(
        tx["amount"] * multiplier,
        2
    )

    tx["amount_anomaly"] = 1

    return tx


def create_subtle_location_anomaly(tx):

    tx = tx.copy()

    countries = [
        "IN",
        "US",
        "GB",
        "CN",
        "NG"
    ]

    current_country = tx["country"]

    alternatives = [
        country
        for country in countries
        if country != current_country
    ]

    tx["country"] = random.choice(
        alternatives
    )

    tx["location_change"] = 1

    return tx


def create_subtle_velocity_anomaly(tx):

    tx = tx.copy()

    # Only moderately higher velocity
    tx["velocity_1h"] = random.randint(
        5,
        8
    )

    return tx


def create_payment_method_anomaly(tx):

    tx = tx.copy()

    methods = [
        "CREDIT_CARD",
        "DEBIT_CARD",
        "UPI",
        "NET_BANKING",
        "WALLET"
    ]

    alternatives = [
        method
        for method in methods
        if method != tx["payment_method"]
    ]

    tx["payment_method"] = random.choice(
        alternatives
    )

    tx["new_payment_method"] = 1

    return tx


def create_merchant_anomaly(tx):

    tx = tx.copy()

    categories = [
        "GROCERY",
        "FOOD",
        "TRAVEL",
        "ECOMMERCE",
        "ENTERTAINMENT",
        "HEALTHCARE",
        "EDUCATION",
        "OTHER"
    ]

    alternatives = [
        category
        for category in categories
        if category != tx["merchant_category"]
    ]

    tx["merchant_category"] = random.choice(
        alternatives
    )

    tx["merchant_anomaly"] = 1

    return tx


def create_low_and_slow_attack(tx):

    """
    Creates a transaction with only subtle deviations.
    """

    tx = tx.copy()

    mutation = random.choice([
        create_subtle_amount_anomaly,
        create_subtle_location_anomaly,
        create_subtle_velocity_anomaly,
        create_payment_method_anomaly,
        create_merchant_anomaly
    ])

    tx = mutation(tx)

    tx["is_fraud"] = 1

    tx["attack_type"] = (
        "ADVERSARIAL_LOW_AND_SLOW"
    )

    return tx


def create_multi_signal_subtle_attack(tx):

    """
    Combines two subtle signals instead of many obvious ones.
    """

    tx = tx.copy()

    mutations = random.sample(
        [
            create_subtle_amount_anomaly,
            create_subtle_location_anomaly,
            create_subtle_velocity_anomaly,
            create_payment_method_anomaly,
            create_merchant_anomaly
        ],
        2
    )

    for mutation in mutations:
        tx = mutation(tx)

    tx["is_fraud"] = 1

    tx["attack_type"] = (
        "ADVERSARIAL_MULTI_SIGNAL"
    )

    return tx


def generate_adversarial_dataset(
    number_of_attacks=2000
):

    legitimate = pd.read_csv(
        INPUT_FILE
    )

    legitimate = legitimate[
        legitimate["is_fraud"] == 0
    ].copy()

    attacks = []

    for _ in range(number_of_attacks):

        transaction = legitimate.sample(
            1
        ).iloc[0]

        attack_type = random.choice([
            "LOW_AND_SLOW",
            "MULTI_SIGNAL"
        ])

        if attack_type == "LOW_AND_SLOW":

            mutated = create_low_and_slow_attack(
                transaction
            )

        else:

            mutated = create_multi_signal_subtle_attack(
                transaction
            )

        attacks.append(mutated)

    return pd.DataFrame(attacks)


def main():

    print()
    print("=" * 60)
    print("RED TEAM — ADVERSARIAL ATTACK MUTATION")
    print("=" * 60)
    print()

    df = generate_adversarial_dataset()

    df.to_csv(
        OUTPUT_FILE,
        index=False
    )

    print(
        f"Generated {len(df):,} adversarial transactions."
    )

    print()

    print("Attack distribution:")

    print(
        df["attack_type"]
        .value_counts()
        .to_string()
    )

    print()

    print("Sample adversarial transactions:")

    print(
        df.head(10).to_string(
            index=False
        )
    )

    print()

    print(
        f"Saved to: {OUTPUT_FILE}"
    )

    print()

    print("=" * 60)
    print("ADVERSARIAL DATASET READY")
    print("=" * 60)


if __name__ == "__main__":
    main()