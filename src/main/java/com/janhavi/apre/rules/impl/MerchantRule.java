package com.janhavi.apre.rules.impl;

import com.janhavi.apre.dto.PaymentRequest;
import com.janhavi.apre.enums.MerchantCategory;
import com.janhavi.apre.rules.RiskResult;
import com.janhavi.apre.rules.RiskRule;
import org.springframework.stereotype.Component;

@Component
public class MerchantRule implements RiskRule {

    @Override
    public void evaluate(PaymentRequest request, RiskResult result) {

        MerchantCategory category = request.getMerchantCategory();

        if (category == null) {
            return;
        }

        switch (category) {

            case TRAVEL -> {
                result.addScore(15);
                result.addReason("Travel merchant.");
            }

            case ENTERTAINMENT -> {
                result.addScore(10);
                result.addReason("Entertainment merchant.");
            }

            case ECOMMERCE -> {
                result.addScore(5);
                result.addReason("E-commerce merchant.");
            }

            default -> {
                // No additional risk
            }
        }
    }
}