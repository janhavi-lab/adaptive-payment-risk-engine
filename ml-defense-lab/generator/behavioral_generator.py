"""
Behavioral Payment Universe Generator

Creates a customer-centric synthetic payment dataset where
transactions can be evaluated relative to historical behavior.

This enables behavioral anomaly detection rather than relying
only on explicit fraud flags.
"""

import random
from pathlib import Path

import numpy as np
import pandas as pd
from faker import Faker


fake = Faker()

random.seed(2026)
np.random.seed(2026)


# ============================================================
# CONFIGURATION
# ============================================================

NUM_CUSTOMERS = 1500
TRANSACTIONS_PER_CUSTOMER = 8

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


# ============================================================
# CUSTOMER PROFILE
# ============================================================

def create_customer(customer_id):

    return {
        "customer_id": customer_id,

        "account_age_days":
            random.randint(
                90,
                2500
            ),

        "usual_country":
            random.choice(
                COUNTRIES
            ),

        "usual_merchant_category":
            random.choice(
                MERCHANT_CATEGORIES
            ),

        "usual_payment_method":
            random.choice(
                PAYMENT_METHODS
            ),

        "usual_amount":
            round(
                np.random.lognormal(
                    mean=7.2,
                    sigma=0.6
                ),
                2
            ),

        "usual_velocity":
            random.randint(
                1,
                3
            )
    }


# ============================================================
# LEGITIMATE TRANSACTION
# ============================================================

def generate_normal_transaction(
    profile
):

    amount = np.random.normal(
        profile["usual_amount"],
        profile["usual_amount"] * 0.20
    )

    amount = max(
        50,
        amount
    )

    velocity = max(
        1,
        int(
            np.random.normal(
                profile["usual_velocity"],
                1
            )
        )
    )

    return {

        "customer_id":
            profile["customer_id"],

        "amount":
            round(
                amount,
                2
            ),

        "merchant_name":
            fake.company(),

        "merchant_category":
            profile[
                "usual_merchant_category"
            ],

        "country":
            profile[
                "usual_country"
            ],

        "payment_method":
            profile[
                "usual_payment_method"
            ],

        "velocity_1h":
            velocity,

        "device_age_days":
            random.randint(
                60,
                1000
            ),

        "account_age_days":
            profile[
                "account_age_days"
            ],

        "is_fraud":
            0,

        "attack_type":
            "LEGITIMATE"
    }


# ============================================================
# SUBTLE LOW-AND-SLOW ATTACK
# ============================================================

def generate_low_slow_attack(
    profile
):

    """
    Creates an attack that remains close to the customer's
    normal behavioral profile.

    Only behavioral intensity is changed.
    """

    amount_multiplier = random.uniform(
        1.35,
        2.2
    )

    velocity_multiplier = random.uniform(
        1.8,
        3.0
    )

    amount = (
        profile["usual_amount"]
        * amount_multiplier
    )

    velocity = int(
        profile["usual_velocity"]
        * velocity_multiplier
    )

    return {

        "customer_id":
            profile["customer_id"],

        "amount":
            round(
                amount,
                2
            ),

        "merchant_name":
            fake.company(),

        "merchant_category":
            profile[
                "usual_merchant_category"
            ],

        "country":
            profile[
                "usual_country"
            ],

        "payment_method":
            profile[
                "usual_payment_method"
            ],

        "velocity_1h":
            max(
                velocity,
                profile["usual_velocity"] + 2
            ),

        "device_age_days":
            random.randint(
                60,
                1000
            ),

        "account_age_days":
            profile[
                "account_age_days"
            ],

        "is_fraud":
            1,

        "attack_type":
            "LOW_AND_SLOW_BEHAVIORAL"
    }


# ============================================================
# FEATURE ENGINEERING
# ============================================================

def add_behavioral_features(
    df
):

    df = df.copy()

    # --------------------------------------------------------
    # Amount deviation from customer's normal amount
    # --------------------------------------------------------

    customer_amount_baseline = (
        df.groupby(
            "customer_id"
        )["amount"]
        .transform("median")
    )

    df[
        "amount_deviation_ratio"
    ] = (
        df["amount"]
        / customer_amount_baseline.clip(
            lower=1
        )
    )

    # --------------------------------------------------------
    # Velocity deviation
    # --------------------------------------------------------

    customer_velocity_baseline = (
        df.groupby(
            "customer_id"
        )["velocity_1h"]
        .transform("median")
    )

    df[
        "velocity_deviation_ratio"
    ] = (
        df["velocity_1h"]
        / customer_velocity_baseline.clip(
            lower=1
        )
    )

    # --------------------------------------------------------
    # Behavioral risk score
    # --------------------------------------------------------

    df[
        "behavioral_deviation_score"
    ] = (

        df[
            "amount_deviation_ratio"
        ].clip(
            upper=5
        )

        +

        df[
            "velocity_deviation_ratio"
        ].clip(
            upper=5
        )

    ) / 2

    return df


# ============================================================
# DATASET GENERATION
# ============================================================

def generate_dataset():

    rows = []

    for customer_number in range(
        NUM_CUSTOMERS
    ):

        customer_id = (
            f"CUST_{customer_number:05d}"
        )

        profile = create_customer(
            customer_id
        )

        for _ in range(
            TRANSACTIONS_PER_CUSTOMER
        ):

            # Most transactions legitimate
            if random.random() < 0.85:

                transaction = (
                    generate_normal_transaction(
                        profile
                    )
                )

            else:

                transaction = (
                    generate_low_slow_attack(
                        profile
                    )
                )

            rows.append(
                transaction
            )

    df = pd.DataFrame(
        rows
    )

    df = add_behavioral_features(
        df
    )

    return df


# ============================================================
# MAIN
# ============================================================

def main():

    print()
    print("=" * 65)
    print("BEHAVIORAL PAYMENT UNIVERSE")
    print("=" * 65)
    print()

    df = generate_dataset()

    output_file = (
        Path(__file__).resolve().parent.parent
        / "data"
        / "behavioral_payments.csv"
    )

    df.to_csv(
        output_file,
        index=False
    )

    print(
        f"Customers: {NUM_CUSTOMERS:,}"
    )

    print(
        f"Transactions: {len(df):,}"
    )

    print()

    print(
        "Transaction distribution:"
    )

    print(
        df["attack_type"]
        .value_counts()
        .to_string()
    )

    print()

    print(
        "Behavioral feature summary:"
    )

    print(
        df[
            [
                "amount_deviation_ratio",
                "velocity_deviation_ratio",
                "behavioral_deviation_score"
            ]
        ]
        .describe()
        .round(2)
        .to_string()
    )

    print()

    print(
        f"Saved to:\n{output_file}"
    )

    print()

    print("=" * 65)
    print("BEHAVIORAL DATASET READY")
    print("=" * 65)
    print()


if __name__ == "__main__":
    main()