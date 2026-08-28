package com.janhavi.apre.controller;

import com.janhavi.apre.dto.PaymentRequest;
import com.janhavi.apre.dto.PaymentResponse;
import com.janhavi.apre.entity.PaymentTransaction;
import com.janhavi.apre.enums.Decision;
import com.janhavi.apre.enums.RiskCategory;
import com.janhavi.apre.service.RiskEngineService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final RiskEngineService riskEngineService;

    public PaymentController(RiskEngineService riskEngineService) {
        this.riskEngineService = riskEngineService;
    }

    // ============================================================
    // Evaluate Payment
    // Accessible by ADMIN and ANALYST
    // ============================================================
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST')")
    @PostMapping("/evaluate")
    public PaymentResponse evaluatePayment(
            @Valid @RequestBody PaymentRequest request) {

        return riskEngineService.evaluate(request);
    }

    // ============================================================
    // Get Transactions
    // Accessible by ADMIN and ANALYST
    // ============================================================
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST')")
    @GetMapping
    public Page<PaymentTransaction> getTransactions(

            @RequestParam(defaultValue = "0") int page,

            @RequestParam(defaultValue = "10") int size,

            @RequestParam(defaultValue = "paymentTime") String sortBy,

            @RequestParam(defaultValue = "desc") String direction,

            @RequestParam(required = false) RiskCategory riskCategory,

            @RequestParam(required = false) Decision decision) {

        return riskEngineService.getTransactions(
                page,
                size,
                sortBy,
                direction,
                riskCategory,
                decision
        );
    }
}