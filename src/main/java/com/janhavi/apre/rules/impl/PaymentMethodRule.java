package com.janhavi.apre.rules.impl;

import com.janhavi.apre.dto.PaymentRequest;
import com.janhavi.apre.enums.PaymentMethod;
import com.janhavi.apre.rules.RiskResult;
import com.janhavi.apre.rules.RiskRule;
import org.springframework.stereotype.Component;

@Component
public class PaymentMethodRule implements RiskRule {

    @Override
    public void evaluate(PaymentRequest request, RiskResult result) {

        PaymentMethod method = request.getPaymentMethod();

        if (method == null) {
            return;
        }

        switch (method) {

            case CREDIT_CARD -> {
                result.addScore(10);
                result.addReason("Credit card transaction.");
            }

            case WALLET -> {
                result.addScore(5);
                result.addReason("Wallet payment.");
            }

            case NET_BANKING -> {
                result.addScore(5);
                result.addReason("Net Banking payment.");
            }

            case DEBIT_CARD, UPI -> {
                // No additional risk score
            }
        }
    }
}