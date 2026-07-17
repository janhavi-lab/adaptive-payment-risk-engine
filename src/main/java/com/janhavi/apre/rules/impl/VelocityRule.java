package com.janhavi.apre.rules.impl;

import com.janhavi.apre.dto.PaymentRequest;
import com.janhavi.apre.rules.RiskResult;
import com.janhavi.apre.rules.RiskRule;
import org.springframework.stereotype.Component;

@Component
public class VelocityRule implements RiskRule {

    @Override
    public void evaluate(PaymentRequest request, RiskResult result) {

        if (request.getAmount().doubleValue() > 200000) {

            result.addScore(15);
            result.addReason("Very large payment may require manual review.");

        }

    }

}