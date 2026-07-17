package com.janhavi.apre.service;

import com.janhavi.apre.dto.PaymentRequest;
import com.janhavi.apre.dto.PaymentResponse;
import com.janhavi.apre.entity.PaymentTransaction;
import com.janhavi.apre.enums.Decision;
import com.janhavi.apre.enums.RiskCategory;
import com.janhavi.apre.repository.PaymentRepository;
import com.janhavi.apre.rules.RiskResult;
import com.janhavi.apre.rules.RiskRule;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class RiskEngineService {

    private final List<RiskRule> rules;
    private final PaymentRepository paymentRepository;

    public RiskEngineService(List<RiskRule> rules,
                             PaymentRepository paymentRepository) {
        this.rules = rules;
        this.paymentRepository = paymentRepository;
    }

    public PaymentResponse evaluate(PaymentRequest request) {

        RiskResult result = new RiskResult();

        for (RiskRule rule : rules) {
            rule.evaluate(request, result);
        }

        PaymentResponse response = new PaymentResponse();

        response.setTransactionId(UUID.randomUUID());
        response.setRiskScore(result.getScore());
        response.setReasons(result.getReasons());

        if (result.getScore() < 20) {

            response.setRiskCategory(RiskCategory.LOW);
            response.setDecision(Decision.APPROVED);

        } else if (result.getScore() < 50) {

            response.setRiskCategory(RiskCategory.MEDIUM);
            response.setDecision(Decision.APPROVED_WITH_MONITORING);

        } else if (result.getScore() < 80) {

            response.setRiskCategory(RiskCategory.HIGH);
            response.setDecision(Decision.MANUAL_REVIEW);

        } else {

            response.setRiskCategory(RiskCategory.CRITICAL);
            response.setDecision(Decision.DECLINED);

        }

        // Save transaction to database
        PaymentTransaction transaction = new PaymentTransaction();

        transaction.setTransactionId(response.getTransactionId());
        transaction.setAmount(request.getAmount());
        transaction.setMerchantName(request.getMerchantName());
        transaction.setMerchantCategory(request.getMerchantCategory());
        transaction.setCountry(request.getCountry());
        transaction.setPaymentMethod(request.getPaymentMethod());

        transaction.setRiskScore(response.getRiskScore());
        transaction.setRiskCategory(response.getRiskCategory());
        transaction.setDecision(response.getDecision());
        transaction.setReasons(String.join(", ", response.getReasons()));

        paymentRepository.save(transaction);

        return response;
    }
}