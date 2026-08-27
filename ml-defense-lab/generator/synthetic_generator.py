"""
Synthetic Payment Transaction Generator

Generates realistic defensive payment telemetry for the
Adaptive Adversarial Payment Defense Lab.

The generated data is synthetic and intended only for
fraud-detection research and model evaluation.
"""

import random
from pathlib import Path

import numpy as np
import pandas as pd
from faker import Faker


fake = Faker()
random.seed(42)
np.random.seed(42)


# ============================================================
# CONFIGURATION
# ============================================================

NUM_TRANSACTIONS = 10000

COUNTRIES = [
    "IN",
    "US",
    "GB",
    "CN",
    "NG"
]

MERCHANT_CATEGORIES = [
    "GROCERY",
    "FOOD",
    "TRAVEL",
    "ECOMMERCE",
    "ENTERTAINMENT",
    "HEALTHCARE",
    "EDUCATION",
    "OTHER"
]

PAYMENT_METHODS = [
    "CREDIT_CARD",
    "DEBIT_CARD",
    "UPI",
    "NET_BANKING",
    "WALLET"
]

ATTACK_TYPES = [
    "ACCOUNT_TAKEOVER",
    "SYNTHETIC_IDENTITY",
    "TRANSACTION_ANOMALY",
    "VELOCITY_ABUSE",
    "GEOGRAPHIC_ANOMALY",
    "MERCHANT_ANOMALY",
    "PAYMENT_METHOD_ABUSE",
    "MULTI_SIGNAL_ATTACK"
]


# ============================================================
# HELPER FUNCTIONS
# ============================================================

def random_amount():
    """
    Generate a realistic payment amount.
    Most legitimate transactions are relatively small.
    """

    amount = np.random.lognormal(
        mean=6.0,
        sigma=1.0
    )

    return round(float(np.clip(amount, 50, 50000)), 2)


def random_country():
    return random.choice(COUNTRIES)


def random_merchant_category():
    return random.choice(MERCHANT_CATEGORIES)


def random_payment_method():
    return random.choice(PAYMENT_METHODS)


def generate_customer_profile():

    return {
        "customer_id": f"CUST_{random.randint(10000, 99999)}",

        "account_age_days": random.randint(
            30,
            2500
        ),

        "usual_country": random_country(),

        "usual_merchant_category":
            random_merchant_category(),

        "usual_payment_method":
            random_payment_method(),

        "usual_amount":
            round(
                random.uniform(200, 5000),
                2
            )
    }


# ============================================================
# LEGITIMATE TRANSACTION
# ============================================================

def generate_legitimate_transaction(profile):

    usual_amount = profile["usual_amount"]

    amount = np.random.normal(
        usual_amount,
        max(100, usual_amount * 0.25)
    )

    amount = float(
        np.clip(
            amount,
            50,
            15000
        )
    )

    return {

        "customer_id":
            profile["customer_id"],

        "amount":
            round(amount, 2),

        "merchant_name":
            fake.company(),

        "merchant_category":
            profile["usual_merchant_category"],

        "country":
            profile["usual_country"],

        "payment_method":
            profile["usual_payment_method"],

        "velocity_1h":
            random.randint(1, 4),

        "device_age_days":
            random.randint(30, 1000),

        "location_change":
            0,

        "new_payment_method":
            0,

        "account_age_days":
            profile["account_age_days"],

        "merchant_anomaly":
            0,

        "amount_anomaly":
            0,

        "is_fraud":
            0,

        "attack_type":
            "LEGITIMATE"
    }


# ============================================================
# ATTACK GENERATORS
# ============================================================

def apply_account_takeover(tx, profile):

    tx["amount"] = round(
        profile["usual_amount"] *
        random.uniform(2.5, 8),
        2
    )

    tx["country"] = random.choice(
        [
            c for c in COUNTRIES
            if c != profile["usual_country"]
        ]
    )

    tx["velocity_1h"] = random.randint(
        5,
        15
    )

    tx["device_age_days"] = random.randint(
        0,
        3
    )

    tx["location_change"] = 1
    tx["new_payment_method"] = 1
    tx["amount_anomaly"] = 1
    tx["merchant_anomaly"] = 1

    return tx


def apply_synthetic_identity(tx, profile):

    tx["account_age_days"] = random.randint(
        1,
        30
    )

    tx["amount"] = random.uniform(
        3000,
        30000
    )

    tx["merchant_category"] = random.choice(
        MERCHANT_CATEGORIES
    )

    tx["payment_method"] = random.choice(
        PAYMENT_METHODS
    )

    tx["velocity_1h"] = random.randint(
        3,
        10
    )

    tx["device_age_days"] = random.randint(
        0,
        10
    )

    tx["merchant_anomaly"] = 1
    tx["amount_anomaly"] = 1
    tx["new_payment_method"] = 1

    return tx


def apply_transaction_anomaly(tx, profile):

    tx["amount"] = round(
        profile["usual_amount"] *
        random.uniform(5, 15),
        2
    )

    tx["amount"] = min(
        tx["amount"],
        100000
    )

    tx["amount_anomaly"] = 1

    tx["merchant_category"] = random.choice(
        MERCHANT_CATEGORIES
    )

    if (
        tx["merchant_category"]
        != profile["usual_merchant_category"]
    ):
        tx["merchant_anomaly"] = 1

    return tx


def apply_velocity_abuse(tx, profile):

    tx["velocity_1h"] = random.randint(
        10,
        30
    )

    tx["amount"] = random.uniform(
        500,
        10000
    )

    return tx


def apply_geographic_anomaly(tx, profile):

    tx["country"] = random.choice(
        [
            c for c in COUNTRIES
            if c != profile["usual_country"]
        ]
    )

    tx["location_change"] = 1

    return tx


def apply_merchant_anomaly(tx, profile):

    unusual_categories = [
        c for c in MERCHANT_CATEGORIES
        if c != profile["usual_merchant_category"]
    ]

    tx["merchant_category"] = random.choice(
        unusual_categories
    )

    tx["merchant_anomaly"] = 1

    return tx


def apply_payment_method_abuse(tx, profile):

    unusual_methods = [
        method
        for method in PAYMENT_METHODS
        if method != profile["usual_payment_method"]
    ]

    tx["payment_method"] = random.choice(
        unusual_methods
    )

    tx["new_payment_method"] = 1

    return tx


def apply_multi_signal_attack(tx, profile):

    tx = apply_account_takeover(
        tx,
        profile
    )

    tx["velocity_1h"] = random.randint(
        8,
        25
    )

    tx["amount"] = round(
        profile["usual_amount"] *
        random.uniform(4, 12),
        2
    )

    return tx


# ============================================================
# ATTACK DISPATCHER
# ============================================================

def apply_attack(tx, profile, attack_type):

    if attack_type == "ACCOUNT_TAKEOVER":
        return apply_account_takeover(
            tx,
            profile
        )

    if attack_type == "SYNTHETIC_IDENTITY":
        return apply_synthetic_identity(
            tx,
            profile
        )

    if attack_type == "TRANSACTION_ANOMALY":
        return apply_transaction_anomaly(
            tx,
            profile
        )

    if attack_type == "VELOCITY_ABUSE":
        return apply_velocity_abuse(
            tx,
            profile
        )

    if attack_type == "GEOGRAPHIC_ANOMALY":
        return apply_geographic_anomaly(
            tx,
            profile
        )

    if attack_type == "MERCHANT_ANOMALY":
        return apply_merchant_anomaly(
            tx,
            profile
        )

    if attack_type == "PAYMENT_METHOD_ABUSE":
        return apply_payment_method_abuse(
            tx,
            profile
        )

    if attack_type == "MULTI_SIGNAL_ATTACK":
        return apply_multi_signal_attack(
            tx,
            profile
        )

    return tx


# ============================================================
# DATASET GENERATOR
# ============================================================

def generate_dataset(num_transactions=NUM_TRANSACTIONS):

    rows = []

    for _ in range(num_transactions):

        profile = generate_customer_profile()

        # ----------------------------------------------------
        # 65% legitimate transactions
        # 35% adversarial transactions
        # ----------------------------------------------------

        if random.random() < 0.65:

            transaction = generate_legitimate_transaction(
                profile
            )

        else:

            transaction = generate_legitimate_transaction(
                profile
            )

            attack_type = random.choice(
                ATTACK_TYPES
            )

            transaction = apply_attack(
                transaction,
                profile,
                attack_type
            )

            transaction["is_fraud"] = 1
            transaction["attack_type"] = attack_type

        rows.append(transaction)

    return pd.DataFrame(rows)


# ============================================================
# SAVE DATASET
# ============================================================

def main():

    print()
    print("=" * 60)
    print("ADAPTIVE ADVERSARIAL PAYMENT DEFENSE LAB")
    print("Synthetic Payment Universe Generator")
    print("=" * 60)
    print()

    df = generate_dataset()

    output_directory = (
        Path(__file__).resolve().parent.parent / "data"
    )

    output_directory.mkdir(
        parents=True,
        exist_ok=True
    )

    output_file = (
        output_directory /
        "synthetic_payments.csv"
    )

    df.to_csv(
        output_file,
        index=False
    )

    print(
        f"Generated {len(df):,} transactions."
    )

    print()
    print("Dataset distribution:")
    print(
        df["attack_type"]
        .value_counts()
        .to_string()
    )

    print()
    print("Fraud distribution:")
    print(
        df["is_fraud"]
        .value_counts()
        .to_string()
    )

    print()
    print("Dataset shape:")
    print(df.shape)

    print()
    print("Sample transactions:")
    print(
        df.head(10).to_string(
            index=False
        )
    )

    print()
    print(
        f"Saved to: {output_file}"
    )

    print()
    print("=" * 60)
    print("SYNTHETIC DATASET READY")
    print("=" * 60)


if __name__ == "__main__":
    main()