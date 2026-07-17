package com.janhavi.apre.repository;

import com.janhavi.apre.entity.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentTransaction, Long> {

    boolean existsByTransactionId(UUID transactionId);

}