package com.smartretail.backend.controller;

import com.smartretail.backend.service.ReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

@RestController
@RequestMapping("/api/reports")
public class ReportController {
    private static final Logger logger = LoggerFactory.getLogger(ReportController.class);
    private final ReportService reportService;
    private final MessageSource messageSource;

    public ReportController(ReportService reportService, MessageSource messageSource) {
        this.reportService = reportService;
        this.messageSource = messageSource;
    }

    @GetMapping("/sales")
    public ResponseEntity<Map<String, Object>> getSalesReport(
            @RequestParam String startDate,
            @RequestParam String endDate,
            Locale locale) {
        logger.debug("Fetching sales report: startDate={}, endDate={}", startDate, endDate);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC")); // Parse dates in UTC
        dateFormat.setLenient(false);
        try {
            Date start = dateFormat.parse(startDate);
            Date end = dateFormat.parse(endDate);
            // Adjust endDate to include the full day
            end = new Date(end.getTime() + 24 * 60 * 60 * 1000 - 1); // End of day
            Map<String, Object> report = reportService.getSalesReport(start, end, locale);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            logger.error("Invalid date format: startDate={} or endDate={}", startDate, endDate, e);
            throw new IllegalArgumentException(
                    messageSource.getMessage("report.date.invalid", new Object[]{startDate + " or " + endDate}, locale));
        }
    }

    @GetMapping("/inventory")
    public ResponseEntity<Map<String, Object>> getInventoryReport(
            @RequestParam(defaultValue = "10") int lowStockThreshold,
            @RequestParam(defaultValue = "30") int expiryDays,
            Locale locale) {
        logger.debug("Fetching inventory report: lowStockThreshold={}, expiryDays={}", lowStockThreshold, expiryDays);
        Map<String, Object> report = reportService.getInventoryReport(lowStockThreshold, expiryDays, locale);
        return ResponseEntity.ok(report);
    }
}