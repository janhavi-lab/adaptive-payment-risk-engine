package com.janhavi.apre.controller;

import com.janhavi.apre.entity.SecurityTest;
import com.janhavi.apre.service.SecurityDefenseService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/security")
public class SecurityController {

    private static final Logger log = LoggerFactory.getLogger(SecurityController.class);

    private final SecurityDefenseService securityDefenseService;

    public SecurityController(SecurityDefenseService securityDefenseService) {
        this.securityDefenseService = securityDefenseService;
    }

    // ============================================================
    // ATTACK MY DEFENCE: Trigger controlled adversarial attack test
    // Accessible by ADMIN and ANALYST
    // ============================================================
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST')")
    @PostMapping("/attack")
    public ResponseEntity<?> triggerAttack(
            @RequestBody(required = false) Map<String, Object> request) {

        String attackType = "AUTO";
        int batchSize = 50;

        if (request != null) {
            if (request.containsKey("attackType") && request.get("attackType") != null) {
                attackType = request.get("attackType").toString();
            } else if (request.containsKey("attack_type") && request.get("attack_type") != null) {
                attackType = request.get("attack_type").toString();
            }

            if (request.containsKey("batchSize") && request.get("batchSize") instanceof Number) {
                batchSize = ((Number) request.get("batchSize")).intValue();
            } else if (request.containsKey("batch_size") && request.get("batch_size") instanceof Number) {
                batchSize = ((Number) request.get("batch_size")).intValue();
            }
        }

        try {
            Map<String, Object> result = securityDefenseService.triggerAttack(attackType, batchSize);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Security attack execution error for scenario '{}': {}", attackType, e.getMessage(), e);
            Map<String, Object> errorBody = new java.util.LinkedHashMap<>();
            errorBody.put("error", "Defense simulation is temporarily unavailable.");
            errorBody.put("message", "Defense simulation is temporarily unavailable. Please retry.");
            return ResponseEntity.status(503).body(errorBody);
        }
    }


    // ============================================================
    // Get Security Test History
    // Accessible by ADMIN and ANALYST
    // ============================================================
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST')")
    @GetMapping("/tests")
    public ResponseEntity<Page<SecurityTest>> getRecentTests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(securityDefenseService.getRecentTests(page, size));
    }

    // ============================================================
    // Get Attack Intelligence Scenario Catalogue
    // Accessible by ADMIN and ANALYST
    // ============================================================
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST')")
    @GetMapping("/scenarios")
    public ResponseEntity<List<Map<String, Object>>> getScenarios() {
        return ResponseEntity.ok(securityDefenseService.getScenarios());
    }

    // ============================================================
    // Get Aggregate System Defense Metrics
    // Accessible by ADMIN and ANALYST
    // ============================================================
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST')")
    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> getMetrics() {
        return ResponseEntity.ok(securityDefenseService.getSystemDefenseMetrics());
    }
}
