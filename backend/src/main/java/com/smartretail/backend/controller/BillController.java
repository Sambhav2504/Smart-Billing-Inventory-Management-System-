package com.smartretail.backend.controller;

import com.smartretail.backend.models.Bill;
import com.smartretail.backend.service.BillService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/billing")
public class BillController {
    private final BillService billService;

    public BillController(BillService billService) {
        this.billService = billService;
    }

    @PostMapping
    public ResponseEntity<Bill> createBill(@Valid @RequestBody Bill bill) {
        Bill savedBill = billService.createBill(bill);
        return ResponseEntity.status(201).body(savedBill);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Bill> getBillById(@PathVariable("id") String billId) {
        Bill bill = billService.getBillById(billId);
        return ResponseEntity.ok(bill);
    }
}