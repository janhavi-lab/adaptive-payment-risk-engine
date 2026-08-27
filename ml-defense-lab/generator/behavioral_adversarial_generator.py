"""
ADAPTIVE ADVERSARIAL PAYMENT DEFENSE LAB

RED TEAM — MULTI-ATTACK BEHAVIORAL GENERATOR

Attack families:

1. LOW_AND_SLOW_BEHAVIORAL
2. PAYMENT_METHOD_ABUSE
3. ACCOUNT_TAKEOVER
4. MERCHANT_ANOMALY
5. MULTI_SIGNAL_ATTACK

All attacks are generated against customers with known
behavioral baselines.
"""

import random
from pathlib import Path

import numpy as np
import pandas as pd
from faker import Faker


# ============================================================
# INITIALIZATION
# ============================================================

fake = Faker()

random.seed(2026)
np.random.seed(2026)


# ============================================================
# CONFIGURATION
# ============================================================

NUM_CUSTOMERS = 1000

ATTACKS_PER_FAMILY = 2000

TOTAL_ATTACKS = (
    ATTACKS_PER_FAMILY * 5
)

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

        "customer_id":
            customer_id,

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
            ),

        "usual_device_age":
            random.randint(
                100,
                1000
            )
    }


# ============================================================
# CUSTOMER HISTORY
# ============================================================

def generate_customer_history(
    profile,
    history_size=10
):

    rows = []

    for _ in range(history_size):

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
                    0.7
                )
            )
        )

        rows.append({

            "customer_id":
                profile["customer_id"],

            "amount":
                round(
                    amount,
                    2
                ),

            "velocity_1h":
                velocity,

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

            "device_age_days":
                profile[
                    "usual_device_age"
                ],

            "account_age_days":
                profile[
                    "account_age_days"
                ],

            "is_fraud":
                0
        })

    return rows


# ============================================================
# ATTACK 1 — LOW AND SLOW
# ============================================================

def low_and_slow(
    profile,
    baseline_amount,
    baseline_velocity
):

    amount = (
        baseline_amount
        * random.uniform(
            1.35,
            2.0
        )
    )

    velocity = (
        baseline_velocity
        + random.choice(
            [2, 3]
        )
    )

    return {

        "amount":
            round(
                amount,
                2
            ),

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
            profile[
                "usual_device_age"
            ],

        "attack_type":
            "LOW_AND_SLOW_BEHAVIORAL"
    }


# ============================================================
# ATTACK 2 — PAYMENT METHOD ABUSE
# ============================================================

def payment_method_abuse(
    profile,
    baseline_amount,
    baseline_velocity
):

    alternative_methods = [
        method
        for method in PAYMENT_METHODS
        if method
        != profile[
            "usual_payment_method"
        ]
    ]

    new_method = random.choice(
        alternative_methods
    )

    return {

        "amount":
            round(
                baseline_amount
                * random.uniform(
                    1.1,
                    1.6
                ),
                2
            ),

        "merchant_category":
            profile[
                "usual_merchant_category"
            ],

        "country":
            profile[
                "usual_country"
            ],

        "payment_method":
            new_method,

        "velocity_1h":
            max(
                1,
                baseline_velocity
                + random.choice(
                    [1, 2]
                )
            ),

        "device_age_days":
            profile[
                "usual_device_age"
            ],

        "attack_type":
            "PAYMENT_METHOD_ABUSE"
    }


# ============================================================
# ATTACK 3 — ACCOUNT TAKEOVER
# ============================================================

def account_takeover(
    profile,
    baseline_amount,
    baseline_velocity
):

    alternative_countries = [
        country
        for country in COUNTRIES
        if country
        != profile[
            "usual_country"
        ]
    ]

    alternative_methods = [
        method
        for method in PAYMENT_METHODS
        if method
        != profile[
            "usual_payment_method"
        ]
    ]

    return {

        "amount":
            round(
                baseline_amount
                * random.uniform(
                    1.4,
                    2.2
                ),
                2
            ),

        "merchant_category":
            random.choice(
                MERCHANT_CATEGORIES
            ),

        "country":
            random.choice(
                alternative_countries
            ),

        "payment_method":
            random.choice(
                alternative_methods
            ),

        "velocity_1h":
            baseline_velocity
            + random.randint(
                3,
                5
            ),

        "device_age_days":
            random.randint(
                1,
                30
            ),

        "attack_type":
            "ACCOUNT_TAKEOVER"
    }


# ============================================================
# ATTACK 4 — MERCHANT ANOMALY
# ============================================================

def merchant_anomaly(
    profile,
    baseline_amount,
    baseline_velocity
):

    alternative_categories = [
        category
        for category in MERCHANT_CATEGORIES
        if category
        != profile[
            "usual_merchant_category"
        ]
    ]

    return {

        "amount":
            round(
                baseline_amount
                * random.uniform(
                    1.2,
                    1.8
                ),
                2
            ),

        "merchant_category":
            random.choice(
                alternative_categories
            ),

        "country":
            profile[
                "usual_country"
            ],

        "payment_method":
            profile[
                "usual_payment_method"
            ],

        "velocity_1h":
            baseline_velocity
            + random.choice(
                [1, 2]
            ),

        "device_age_days":
            profile[
                "usual_device_age"
            ],

        "attack_type":
            "MERCHANT_ANOMALY"
    }


# ============================================================
# ATTACK 5 — MULTI SIGNAL
# ============================================================

def multi_signal_attack(
    profile,
    baseline_amount,
    baseline_velocity
):

    alternative_countries = [
        country
        for country in COUNTRIES
        if country
        != profile[
            "usual_country"
        ]
    ]

    alternative_methods = [
        method
        for method in PAYMENT_METHODS
        if method
        != profile[
            "usual_payment_method"
        ]
    ]

    alternative_categories = [
        category
        for category in MERCHANT_CATEGORIES
        if category
        != profile[
            "usual_merchant_category"
        ]
    ]

    return {

        "amount":
            round(
                baseline_amount
                * random.uniform(
                    1.6,
                    2.5
                ),
                2
            ),

        "merchant_category":
            random.choice(
                alternative_categories
            ),

        "country":
            random.choice(
                alternative_countries
            ),

        "payment_method":
            random.choice(
                alternative_methods
            ),

        "velocity_1h":
            baseline_velocity
            + random.randint(
                3,
                6
            ),

        "device_age_days":
            random.randint(
                1,
                20
            ),

        "attack_type":
            "MULTI_SIGNAL_ATTACK"
    }


# ============================================================
# FEATURE ENGINEERING
# ============================================================

def add_behavioral_features(
    attacks,
    baselines
):

    attacks = attacks.merge(
        baselines,
        on="customer_id",
        how="left"
    )

    attacks[
        "amount_deviation_ratio"
    ] = (

        attacks["amount"]
        /
        attacks[
            "baseline_amount"
        ].clip(
            lower=1
        )

    )

    attacks[
        "velocity_deviation_ratio"
    ] = (

        attacks["velocity_1h"]
        /
        attacks[
            "baseline_velocity"
        ].clip(
            lower=1
        )

    )

    attacks[
        "behavioral_deviation_score"
    ] = (

        attacks[
            "amount_deviation_ratio"
        ].clip(
            upper=5
        )

        +

        attacks[
            "velocity_deviation_ratio"
        ].clip(
            upper=5
        )

    ) / 2

    attacks.drop(
        columns=[
            "baseline_amount",
            "baseline_velocity"
        ],
        inplace=True
    )

    return attacks


# ============================================================
# GENERATE FAMILY
# ============================================================

def generate_family(
    family_function,
    profiles,
    baselines
):

    rows = []

    customer_ids = list(
        profiles.keys()
    )

    for i in range(
        ATTACKS_PER_FAMILY
    ):

        customer_id = random.choice(
            customer_ids
        )

        profile = profiles[
            customer_id
        ]

        baseline = baselines[
            customer_id
        ]

        attack = family_function(

            profile,

            baseline[
                "baseline_amount"
            ],

            baseline[
                "baseline_velocity"
            ]
        )

        rows.append({

            "customer_id":
                customer_id,

            "amount":
                attack[
                    "amount"
                ],

            "merchant_name":
                fake.company(),

            "merchant_category":
                attack[
                    "merchant_category"
                ],

            "country":
                attack[
                    "country"
                ],

            "payment_method":
                attack[
                    "payment_method"
                ],

            "velocity_1h":
                attack[
                    "velocity_1h"
                ],

            "device_age_days":
                attack[
                    "device_age_days"
                ],

            "account_age_days":
                profile[
                    "account_age_days"
                ],

            "is_fraud":
                1,

            "attack_type":
                attack[
                    "attack_type"
                ]
        })

    return rows


# ============================================================
# MAIN
# ============================================================

def main():

    print()
    print("=" * 65)
    print(
        "ADAPTIVE ADVERSARIAL PAYMENT DEFENSE LAB"
    )
    print(
        "RED TEAM — MULTI-ATTACK GENERATOR"
    )
    print("=" * 65)
    print()

    profiles = {}

    baselines = {}

    baseline_rows = []

    # --------------------------------------------------------
    # Create customers
    # --------------------------------------------------------

    for i in range(
        NUM_CUSTOMERS
    ):

        customer_id = (
            f"CUST_{i:05d}"
        )

        profile = create_customer(
            customer_id
        )

        profiles[
            customer_id
        ] = profile

        history = (
            generate_customer_history(
                profile
            )
        )

        history_df = pd.DataFrame(
            history
        )

        baseline_amount = (
            history_df[
                "amount"
            ].median()
        )

        baseline_velocity = (
            history_df[
                "velocity_1h"
            ].median()
        )

        baselines[
            customer_id
        ] = {

            "baseline_amount":
                baseline_amount,

            "baseline_velocity":
                baseline_velocity
        }

        baseline_rows.append({

            "customer_id":
                customer_id,

            "baseline_amount":
                baseline_amount,

            "baseline_velocity":
                baseline_velocity
        })

    # --------------------------------------------------------
    # Generate attacks
    # --------------------------------------------------------

    attack_rows = []

    attack_families = [

        (
            "LOW_AND_SLOW_BEHAVIORAL",
            low_and_slow
        ),

        (
            "PAYMENT_METHOD_ABUSE",
            payment_method_abuse
        ),

        (
            "ACCOUNT_TAKEOVER",
            account_takeover
        ),

        (
            "MERCHANT_ANOMALY",
            merchant_anomaly
        ),

        (
            "MULTI_SIGNAL_ATTACK",
            multi_signal_attack
        )
    ]

    for family_name, function in attack_families:

        print(
            f"Generating {family_name}..."
        )

        family_rows = generate_family(
            function,
            profiles,
            baselines
        )

        attack_rows.extend(
            family_rows
        )

    attacks_df = pd.DataFrame(
        attack_rows
    )

    baselines_df = pd.DataFrame(
        baseline_rows
    )

    # --------------------------------------------------------
    # Behavioral features
    # --------------------------------------------------------

    attacks_df = add_behavioral_features(
        attacks_df,
        baselines_df
    )

    # --------------------------------------------------------
    # Shuffle
    # --------------------------------------------------------

    attacks_df = (
        attacks_df
        .sample(
            frac=1,
            random_state=2026
        )
        .reset_index(
            drop=True
        )
    )

    # --------------------------------------------------------
    # Save
    # --------------------------------------------------------

    output_file = (
        Path(__file__).resolve().parent.parent
        / "data"
        / "behavioral_adversarial_attacks.csv"
    )

    attacks_df.to_csv(
        output_file,
        index=False
    )

    # --------------------------------------------------------
    # Report
    # --------------------------------------------------------

    print()
    print("=" * 65)
    print("RED TEAM ATTACK UNIVERSE")
    print("=" * 65)
    print()

    print(
        f"Customers: "
        f"{NUM_CUSTOMERS:,}"
    )

    print(
        f"Attacks per family: "
        f"{ATTACKS_PER_FAMILY:,}"
    )

    print(
        f"Total adversarial transactions: "
        f"{len(attacks_df):,}"
    )

    print()

    print(
        "Attack distribution:"
    )

    print(
        attacks_df[
            "attack_type"
        ]
        .value_counts()
        .to_string()
    )

    print()

    print(
        "Behavioral feature summary:"
    )

    print(
        attacks_df[
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
    print(
        "MULTI-ATTACK RED TEAM DATASET READY"
    )
    print("=" * 65)
    print()


if __name__ == "__main__":
    main()