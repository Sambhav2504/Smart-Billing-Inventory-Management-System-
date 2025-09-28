package com.smartretail.backend.controller;

import com.smartretail.backend.service.AnalyticsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {
    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/daily")
    @PreAuthorize("hasAnyRole('OWNER','MANAGER')")
    public ResponseEntity<?> getDailySales() {
        try {
            Object data = analyticsService.getDailySales();
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "Analytics service unavailable: " + e.getMessage()));
        }
    }

    @GetMapping("/monthly")
    @PreAuthorize("hasAnyRole('OWNER','MANAGER')")
    public ResponseEntity<?> getMonthlySales() {
        try {
            Object data = analyticsService.getMonthlySales();
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "Analytics service unavailable: " + e.getMessage()));
        }
    }

    @GetMapping("/top-products")
    @PreAuthorize("hasAnyRole('OWNER','MANAGER')")
    public ResponseEntity<?> getTopProducts() {
        try {
            Object data = analyticsService.getTopProducts();
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "Analytics service unavailable: " + e.getMessage()));
        }
    }

    @GetMapping("/revenue-trend")
    @PreAuthorize("hasAnyRole('OWNER','MANAGER')")
    public ResponseEntity<?> getRevenueTrend() {
        try {
            Object data = analyticsService.getRevenueTrend();
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "Analytics service unavailable: " + e.getMessage()));
        }
    }

    @GetMapping("/report")
    @PreAuthorize("hasAnyRole('OWNER','MANAGER')")
    public ResponseEntity<?> getReport() {
        try {
            Object data = analyticsService.getReport();
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "Analytics service unavailable: " + e.getMessage()));
        }
    }

    @GetMapping("/report/text")
    @PreAuthorize("hasAnyRole('OWNER','MANAGER')")
    public ResponseEntity<?> getTextReport() {
        try {
            Object data = analyticsService.getTextReport();
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "Analytics service unavailable: " + e.getMessage()));
        }
    }
}