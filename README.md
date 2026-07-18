# 🛡️ Adaptive Payment Risk Engine

An AI-inspired payment fraud detection system that evaluates transactions in real time using configurable risk rules. The application analyzes payment details, calculates a risk score, classifies the transaction risk level, and recommends an approval decision.

---

## 🚀 Overview

Adaptive Payment Risk Engine is a full-stack web application built using Spring Boot and React. It simulates how modern payment companies assess transaction risk before approving payments.

The system evaluates transactions using multiple business rules such as:

- Transaction Amount
- Merchant Category
- Country
- Payment Method
- Time of Transaction

Based on these rules, it calculates a risk score and recommends one of the following decisions:

- ✅ Approved
- 👀 Approved With Monitoring
- 📝 Manual Review
- ❌ Declined

The project also includes JWT authentication, role-based access, transaction history, and a responsive dashboard.
