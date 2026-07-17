package com.janhavi.apre.rules;

import com.janhavi.apre.dto.PaymentRequest;

public interface RiskRule {

    void evaluate(PaymentRequest request, RiskResult result);

}