package com.smartretail.backend.controller;

import com.smartretail.backend.models.Bill;
import com.smartretail.backend.service.SyncService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;

@RestController
@RequestMapping("/api/sync")
public class SyncController {

    private final SyncService syncService;

    public SyncController(SyncService syncService) {
        this.syncService = syncService;
    }

    @PostMapping("/bill")
    @PreAuthorize("hasAnyRole('CASHIER', 'MANAGER')")
    public ResponseEntity<String> syncBill(@RequestBody Bill bill, Locale locale) {
        String billId = syncService.syncBill(bill, locale);
        return ResponseEntity.ok("Bill synced: " + billId);
    }
}