package com.janhavi.apre.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "security_tests")
public class SecurityTest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private String testId;

    @Column(nullable = false)
    private String attackType;

    @Column(nullable = false)
    private String attackName;

    @Column(nullable = false)
    private Integer transactionsGenerated;

    @Column(nullable = false)
    private Integer detectedCount;

    @Column(nullable = false)
    private Integer missedCount;

    @Column(nullable = false)
    private Double detectionRate;

    @Column(nullable = false)
    private String defenseStatus;

    @Column(length = 1000)
    private String weaknessIdentified;

    @Column(length = 100)
    private String feedbackCandidateStatus;

    @Column(length = 500)
    private String feedbackStatus;

    @Column(length = 1000)
    private String adaptiveInsight;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public SecurityTest() {
    }

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public String getTestId() {
        return testId;
    }

    public void setTestId(String testId) {
        this.testId = testId;
    }

    public String getAttackType() {
        return attackType;
    }

    public void setAttackType(String attackType) {
        this.attackType = attackType;
    }

    public String getAttackName() {
        return attackName;
    }

    public void setAttackName(String attackName) {
        this.attackName = attackName;
    }

    public Integer getTransactionsGenerated() {
        return transactionsGenerated;
    }

    public void setTransactionsGenerated(Integer transactionsGenerated) {
        this.transactionsGenerated = transactionsGenerated;
    }

    public Integer getDetectedCount() {
        return detectedCount;
    }

    public void setDetectedCount(Integer detectedCount) {
        this.detectedCount = detectedCount;
    }

    public Integer getMissedCount() {
        return missedCount;
    }

    public void setMissedCount(Integer missedCount) {
        this.missedCount = missedCount;
    }

    public Double getDetectionRate() {
        return detectionRate;
    }

    public void setDetectionRate(Double detectionRate) {
        this.detectionRate = detectionRate;
    }

    public String getDefenseStatus() {
        return defenseStatus;
    }

    public void setDefenseStatus(String defenseStatus) {
        this.defenseStatus = defenseStatus;
    }

    public String getWeaknessIdentified() {
        return weaknessIdentified;
    }

    public void setWeaknessIdentified(String weaknessIdentified) {
        this.weaknessIdentified = weaknessIdentified;
    }

    public String getFeedbackCandidateStatus() {
        return feedbackCandidateStatus;
    }

    public void setFeedbackCandidateStatus(String feedbackCandidateStatus) {
        this.feedbackCandidateStatus = feedbackCandidateStatus;
    }

    public String getFeedbackStatus() {
        return feedbackStatus;
    }

    public void setFeedbackStatus(String feedbackStatus) {
        this.feedbackStatus = feedbackStatus;
    }

    public String getAdaptiveInsight() {
        return adaptiveInsight;
    }

    public void setAdaptiveInsight(String adaptiveInsight) {
        this.adaptiveInsight = adaptiveInsight;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

