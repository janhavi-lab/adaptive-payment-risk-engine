from fastapi import FastAPI
from pydantic import BaseModel
import sys
import os

# ---------------------------------------------------------
# PROJECT ROOT
# ---------------------------------------------------------

PROJECT_ROOT = os.path.dirname(
    os.path.dirname(
        os.path.abspath(__file__)
    )
)

if PROJECT_ROOT not in sys.path:
    sys.path.append(PROJECT_ROOT)


# ---------------------------------------------------------
# ADAPTIVE DEFENSE
# ---------------------------------------------------------

from defense.adaptive_defense import evaluate_payment


# ---------------------------------------------------------
# FASTAPI APPLICATION
# ---------------------------------------------------------

app = FastAPI(
    title="Adaptive Adversarial Payment Defense Lab",
    description=(
        "AI-powered adversarial payment fraud detection, "
        "behavioral analysis and adaptive mitigation system."
    ),
    version="1.0"
)


# ---------------------------------------------------------
# PAYMENT REQUEST
# ---------------------------------------------------------

class Payment(BaseModel):

    # Transaction information
    amount: float

    # Transaction behavior
    velocity_1h: int
    device_age_days: int
    account_age_days: int

    # Behavioral ML features
    amount_deviation_ratio: float
    velocity_deviation_ratio: float
    behavioral_deviation_score: float

    # Categorical information
    merchant_category: str
    country: str
    payment_method: str

    # -----------------------------------------------------
    # ADVERSARIAL / RISK SIGNALS
    # -----------------------------------------------------

    payment_method_risk: float = 0.0

    merchant_anomaly: int = 0

    location_change: int = 0

    new_payment_method: int = 0


# ---------------------------------------------------------
# ROOT ENDPOINT
# ---------------------------------------------------------

@app.get("/")
def root():

    return {
        "system": "Adaptive Adversarial Payment Defense Lab",
        "status": "online",
        "version": "1.0"
    }


# ---------------------------------------------------------
# HEALTH CHECK
# ---------------------------------------------------------

@app.get("/health")
def health():

    return {
        "status": "healthy",
        "service": "adaptive-defense-api"
    }


# ---------------------------------------------------------
# PAYMENT EVALUATION
# ---------------------------------------------------------

@app.post("/evaluate")
def evaluate(payment: Payment):

    transaction = payment.model_dump()

    result = evaluate_payment(transaction)

    return result