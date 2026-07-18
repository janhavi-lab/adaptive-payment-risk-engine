package com.janhavi.apre.repository;

import com.janhavi.apre.entity.PaymentTransaction;
import com.janhavi.apre.enums.Decision;
import com.janhavi.apre.enums.RiskCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentTransaction, Long> {

    boolean existsByTransactionId(UUID transactionId);

    Page<PaymentTransaction> findByRiskCategory(
            RiskCategory riskCategory,
            Pageable pageable
    );

    Page<PaymentTransaction> findByDecision(
            Decision decision,
            Pageable pageable
    );

}