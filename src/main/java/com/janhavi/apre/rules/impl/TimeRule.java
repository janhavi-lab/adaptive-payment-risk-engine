package com.janhavi.apre.rules.impl;

import com.janhavi.apre.dto.PaymentRequest;
import com.janhavi.apre.rules.RiskResult;
import com.janhavi.apre.rules.RiskRule;
import org.springframework.stereotype.Component;

import java.time.LocalTime;

@Component
public class TimeRule implements RiskRule {

    @Override
    public void evaluate(PaymentRequest request, RiskResult result) {

        LocalTime time = LocalTime.now();

        if (time.isAfter(LocalTime.of(23,0))
                || time.isBefore(LocalTime.of(5,0))) {

            result.addScore(10);
            result.addReason("Late night transaction.");

        }

    }

}