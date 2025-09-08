package com.smartretail.backend.controller;

import com.smartretail.backend.models.Bill;
import com.smartretail.backend.service.BillService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bills")
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

    @GetMapping("/{billId}")
    public ResponseEntity<Bill> getBillById(@PathVariable String billId) {
        Bill bill = billService.getBillById(billId);
        if (bill == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(bill);
    }

    @GetMapping
    public ResponseEntity<List<Bill>> getAllBills() {
        List<Bill> bills = billService.getAllBills();
        return ResponseEntity.ok(bills);
    }
}