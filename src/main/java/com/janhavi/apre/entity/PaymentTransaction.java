package com.janhavi.apre.entity;

import com.janhavi.apre.enums.Decision;
import com.janhavi.apre.enums.MerchantCategory;
import com.janhavi.apre.enums.PaymentMethod;
import com.janhavi.apre.enums.RiskCategory;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payment_transaction")
public class PaymentTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private UUID transactionId;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private String merchantName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MerchantCategory merchantCategory;

    @Column(nullable = false)
    private String country;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod;

    @Column(nullable = false)
    private LocalDateTime paymentTime;

    private Integer riskScore;

    @Enumerated(EnumType.STRING)
    private RiskCategory riskCategory;

    @Enumerated(EnumType.STRING)
    private Decision decision;

    @Column(length = 1000)
    private String reasons;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public PaymentTransaction() {
    }

    @PrePersist
    public void prePersist() {

        if (transactionId == null) {
            transactionId = UUID.randomUUID();
        }

        createdAt = LocalDateTime.now();
        paymentTime = LocalDateTime.now();
    }

    // =========================
    // Getters and Setters
    // =========================

    public Long getId() {
        return id;
    }

    public UUID getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(UUID transactionId) {
        this.transactionId = transactionId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }

    public MerchantCategory getMerchantCategory() {
        return merchantCategory;
    }

    public void setMerchantCategory(MerchantCategory merchantCategory) {
        this.merchantCategory = merchantCategory;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public LocalDateTime getPaymentTime() {
        return paymentTime;
    }

    public void setPaymentTime(LocalDateTime paymentTime) {
        this.paymentTime = paymentTime;
    }

    public Integer getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(Integer riskScore) {
        this.riskScore = riskScore;
    }

    public RiskCategory getRiskCategory() {
        return riskCategory;
    }

    public void setRiskCategory(RiskCategory riskCategory) {
        this.riskCategory = riskCategory;
    }

    public Decision getDecision() {
        return decision;
    }

    public void setDecision(Decision decision) {
        this.decision = decision;
    }

    public String getReasons() {
        return reasons;
    }

    public void setReasons(String reasons) {
        this.reasons = reasons;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}