package com.smartretail.backend.controller;

import com.smartretail.backend.service.AnalyticsProxyService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {
    private final AnalyticsProxyService proxyService;

    public AnalyticsController(AnalyticsProxyService proxyService) {
        this.proxyService = proxyService;
    }

    @GetMapping("/daily-sales")
    @PreAuthorize("hasAnyRole('OWNER','MANAGER')")
    public ResponseEntity<?> getDailySales() {
        try {
            Object data = proxyService.getFromFlask("/analytics/daily-sales");
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Flask service unavailable: " + e.getMessage());
        }
    }

    @GetMapping("/monthly-sales")
    @PreAuthorize("hasAnyRole('OWNER','MANAGER')")
    public Object getMonthlySales() {
        return proxyService.getFromFlask("/analytics/monthly-sales");
    }

    @GetMapping("/top-products")
    @PreAuthorize("hasAnyRole('OWNER','MANAGER')")
    public Object getTopProducts() {
        return proxyService.getFromFlask("/analytics/top-products");
    }

    @GetMapping("/revenue-trend")
    @PreAuthorize("hasAnyRole('OWNER','MANAGER')")
    public Object getRevenueTrend(@RequestParam("from") String from,
                                  @RequestParam("to") String to) {
        return proxyService.getFromFlask("/analytics/revenue-trend?from=" + from + "&to=" + to);
    }

    @GetMapping("/reports/sales/pdf")
    @PreAuthorize("hasAnyRole('OWNER','MANAGER')")
    public ResponseEntity<byte[]> getSalesReportPdf(@RequestParam("from") String from,
                                                    @RequestParam("to") String to,
                                                    @RequestParam(value = "category", required = false) String category) {
        String path = "/api/reports/sales/pdf?from=" + from + "&to=" + to;
        if (category != null) {
            path += "&category=" + category;
        }

        byte[] pdf = proxyService.getPdfFromFlask(path);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=sales-report.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
