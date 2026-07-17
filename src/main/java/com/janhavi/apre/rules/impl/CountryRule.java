package com.janhavi.apre.rules.impl;

import com.janhavi.apre.dto.PaymentRequest;
import com.janhavi.apre.rules.RiskResult;
import com.janhavi.apre.rules.RiskRule;
import org.springframework.stereotype.Component;

@Component
public class CountryRule implements RiskRule {

    @Override
    public void evaluate(PaymentRequest request, RiskResult result) {

        String country = request.getCountry();

        if (country == null) {
            return;
        }

        switch (country.toUpperCase()) {

            case "NG" -> {
                result.addScore(25);
                result.addReason("Transaction originated from Nigeria.");
            }

            case "PK" -> {
                result.addScore(20);
                result.addReason("Transaction originated from Pakistan.");
            }

            case "RU" -> {
                result.addScore(20);
                result.addReason("Transaction originated from Russia.");
            }
        }
    }
}