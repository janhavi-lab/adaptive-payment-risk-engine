package com.janhavi.apre.rules.impl;

import com.janhavi.apre.dto.PaymentRequest;
import com.janhavi.apre.rules.RiskResult;
import com.janhavi.apre.rules.RiskRule;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class AmountRule implements RiskRule {

    @Override
    public void evaluate(PaymentRequest request, RiskResult result) {

        BigDecimal amount = request.getAmount();

        if (amount.compareTo(new BigDecimal("100000")) > 0) {

            result.addScore(50);
            result.addReason("Very high transaction amount (> INR 100000)");

        }

        else if (amount.compareTo(new BigDecimal("50000")) > 0) {

            result.addScore(30);
            result.addReason("High transaction amount (> INR 50000)");

        }

        else if (amount.compareTo(new BigDecimal("10000")) > 0) {

            result.addScore(10);
            result.addReason("Medium transaction amount (> INR 10000)");

        }

    }

}