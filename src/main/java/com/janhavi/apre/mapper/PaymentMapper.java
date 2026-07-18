package com.janhavi.apre.mapper;

import com.janhavi.apre.dto.PaymentRequest;
import com.janhavi.apre.dto.PaymentResponse;
import com.janhavi.apre.entity.PaymentTransaction;

import java.time.LocalDateTime;

public class PaymentMapper {

    public static PaymentTransaction toEntity(
            PaymentRequest request,
            PaymentResponse response) {

        PaymentTransaction transaction = new PaymentTransaction();

        transaction.setAmount(request.getAmount());
        transaction.setMerchantName(request.getMerchantName());
        transaction.setMerchantCategory(request.getMerchantCategory());
        transaction.setCountry(request.getCountry());
        transaction.setPaymentMethod(request.getPaymentMethod());

        transaction.setRiskScore(response.getRiskScore());
        transaction.setRiskCategory(response.getRiskCategory());
        transaction.setDecision(response.getDecision());

        transaction.setReasons(
                String.join(", ", response.getReasons())
        );

        transaction.setPaymentTime(LocalDateTime.now());

        return transaction;
    }
}