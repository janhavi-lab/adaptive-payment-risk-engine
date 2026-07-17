package com.janhavi.apre.controller;

import com.janhavi.apre.dto.PaymentRequest;
import com.janhavi.apre.dto.PaymentResponse;
import com.janhavi.apre.service.RiskEngineService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final RiskEngineService riskEngineService;

    public PaymentController(RiskEngineService riskEngineService) {
        this.riskEngineService = riskEngineService;
    }

    @PostMapping("/evaluate")
    public PaymentResponse evaluatePayment(@Valid @RequestBody PaymentRequest request) {

        return riskEngineService.evaluate(request);

    }

}