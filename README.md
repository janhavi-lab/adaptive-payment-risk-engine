# RISKFORGE --- Adaptive Adversarial Payment Risk Engine

> **Attack your defence before attackers do.**

RISKFORGE is an adaptive payment-security platform that combines live
payment-risk evaluation with a proactive Red Team → Blue Team
security-testing loop.

Instead of waiting for real attackers to discover weaknesses, RISKFORGE
generates controlled synthetic adversarial payment patterns, routes them
through the same defence pipeline used for live payments, identifies
missed attack patterns, converts them into adaptive feedback, and
enables retesting.

## 🚀 Core USP --- Attack My Defence

**Generate Attack → Test Defence → Discover Weakness → Adapt → Retest**

The platform shows: - 🔴 Synthetic attacks generated - 🔵 Attacks
detected/intercepted - ⚠️ Attacks missed/bypassed - 📊 Detection rate -
🧠 Identified weakness - 🔄 Adaptive feedback - 🎯 Next defence focus -
🔁 Before vs After retest

The goal is simple: **fail safely in a controlled sandbox so the real
payment system can become harder to attack.**

## 🔁 Closed-Loop Architecture

``` text
Live Payment / Security Test
            ↓
Adaptive Risk Orchestrator
            ↓
      🔴 RED TEAM
   Attack Generation
            ↓
   ⚔ Controlled Sandbox
            ↓
      🔵 BLUE TEAM
   Defence Evaluation
            ↓
   ⚠ Weakness Discovery
            ↓
   ↻ Adaptive Feedback
            ↓
   🎯 Next Defence Focus
            ↓
      🔁 Retest Defence
            └────────────→ Defence
```

## 🧩 Main Features

### 🔴 Red Team

Generates controlled synthetic adversarial transactions across: -
`ACCOUNT_TAKEOVER` - `MULTI_SIGNAL_ATTACK` - `SYNTHETIC_IDENTITY` -
`VELOCITY_ABUSE` - `GEOGRAPHIC_ANOMALY` - `PAYMENT_METHOD_ABUSE` -
`MERCHANT_ANOMALY` - `TRANSACTION_ANOMALY`

### 🔵 Blue Team

Evaluates generated transactions through the payment-risk defence
pipeline using rules and ML evaluation when available.

### ⚠️ Weakness Discovery

Shows what attacks were missed instead of hiding failures.

### ↻ Adaptive Feedback

Turns missed patterns into feedback, an adaptive insight, and the next
defence focus.

### 🔁 Retest Defence

Runs the attack again and displays **Before Adaptation vs After
Adaptation** with a resilience delta.

### 💳 Live Payment Sandbox

Evaluates individual transactions and provides an adaptive risk score,
threat level, final security decision, recommended action, and
explainable defence signals.

### 📚 Attack Intelligence Catalogue

Displays the structured threat scenarios used for controlled adversarial
testing.

### 🧾 Security Test Audit

Persists security stress-test history including payloads,
detected/missed counts, detection rate, defence status, weakness,
feedback and next defence focus.

## 🏗️ Technology Stack

  -----------------------------------------------------------------------
  Layer                   Technology              Role
  ----------------------- ----------------------- -----------------------
  Frontend                React + Vite            Interactive security
                                                  command centre

  Backend                 Spring Boot             Payment and security
                                                  orchestration APIs

  ML Defence Lab          Python / FastAPI        Primary adversarial
                                                  simulation/evaluation
                                                  engine

  Fallback Engine         Java                    Keeps adversarial
                                                  testing available if ML
                                                  service is unavailable

  Database                Supabase PostgreSQL     Payment and
                                                  security-test
                                                  persistence

  Authentication          JWT                     Secure analyst/admin
                                                  access

  Communication           REST APIs               Connects frontend,
                                                  orchestrator and
                                                  defence services
  -----------------------------------------------------------------------

## 🧠 Resilient ML Flow

Primary:

``` text
Spring Boot → MlDefenseClient → Python ML Defense Lab
```

Fallback:

``` text
Spring Boot → MlDefenseClient → AdversarialSimulationFallback → Java Defence Rules
```

Simulation responses identify their source using values such as
`ML_DEFENSE_LAB` or `ORCHESTRATOR_FALLBACK`.

## 🔌 API Flow

``` text
POST /api/auth/register
POST /api/auth/login

POST /api/payments/evaluate
GET  /api/payments

POST /api/security/attack
GET  /api/security/tests
GET  /api/security/metrics
```

## 📊 Detection Rate

``` text
Detection Rate =
(Detected Attacks / Total Attacks Evaluated) × 100
```

Example:

``` text
46 detected / 50 evaluated = 92%
```

Defence interpretation:

      Detection Rate Status
  ------------------ -----------------
               ≥ 90% RESILIENT
    ≥ 70% and \< 90% NEEDS ATTENTION
              \< 70% VULNERABLE

## 🗄️ Data Persistence

RISKFORGE uses Supabase PostgreSQL.

-   `payment_transactions` --- live payment evaluation history
-   `security_tests` --- adversarial security-test and closed-loop
    feedback history

## 🔐 Security

JWT authentication and role-aware access protect analyst/admin
workflows.

Never commit: - API keys - database credentials - JWT secrets -
passwords - real card details - `.env` files

## 🧪 Validation

Representative project checks:

``` bash
npm run build
./mvnw clean package -DskipTests
./mvnw test -Dtest=AdversarialSimulationFallbackTest
python -m py_compile defense_api.py
```

The fallback test suite validates batch sizes, scenario targeting,
detection-rate calculation and sample payload formatting.

## 🌍 Impact & Feasibility

Payment fraud evolves continuously. RISKFORGE addresses this by
proactively stress-testing the defence with controlled synthetic attacks
instead of waiting for production fraud to expose gaps.

The modular architecture allows new attack families, defence rules, ML
models and payment channels to be added without replacing the overall
orchestration design.

## 🌐 Links

**Live Demo:** https://adaptive-payment-risk-engine-ui.vercel.app/

**Frontend:**
https://github.com/janhavi-lab/adaptive-payment-risk-engine-ui

**Backend:** https://github.com/janhavi-lab/adaptive-payment-risk-engine

## 🏆 One-Line Vision

> **RISKFORGE doesn't wait for attackers to find the weakness --- it
> attacks its own defence first, learns from every gap, and retests to
> stay ahead.**

**RISKFORGE --- Adaptive Adversarial Payment Risk Engine • Orchestrator
v2.0**

**Attack. Defend. Discover. Adapt. Retest.**
