package com.janhavi.apre.service;

import com.janhavi.apre.dto.PaymentRequest;
import com.janhavi.apre.dto.PaymentResponse;
import com.janhavi.apre.entity.PaymentTransaction;
import com.janhavi.apre.enums.Decision;
import com.janhavi.apre.enums.RiskCategory;
import com.janhavi.apre.mapper.PaymentMapper;
import com.janhavi.apre.repository.PaymentRepository;
import com.janhavi.apre.rules.RiskResult;
import com.janhavi.apre.rules.RiskRule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Sort;

import java.util.List;

@Service
public class RiskEngineService {

    private final List<RiskRule> rules;
    private final PaymentRepository paymentRepository;

    public RiskEngineService(
            List<RiskRule> rules,
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

        // Convert DTO to Entity
        PaymentTransaction transaction = PaymentMapper.toEntity(request, response);

        // Save into PostgreSQL
        paymentRepository.save(transaction);

        // Return generated transactionId to frontend
        response.setTransactionId(transaction.getTransactionId());

        return response;
    }

    // Fetch paginated transactions
    public Page<PaymentTransaction> getTransactions(
            int page,
            int size,
            String sortBy,
            String direction,
            RiskCategory riskCategory,
            Decision decision) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                direction.equalsIgnoreCase("asc")
                        ? Sort.by(sortBy).ascending()
                        : Sort.by(sortBy).descending()
        );

        if (riskCategory != null) {
            return paymentRepository.findByRiskCategory(riskCategory, pageable);
        }

        if (decision != null) {
            return paymentRepository.findByDecision(decision, pageable);
        }

        return paymentRepository.findAll(pageable);
    }
}