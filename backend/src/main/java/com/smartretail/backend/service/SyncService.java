package com.smartretail.backend.service;

import com.smartretail.backend.models.Bill;
import com.smartretail.backend.repository.BillRepository;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Optional;

@Service
public class SyncService {

    private final BillRepository billRepository;
    private final BillService billService;

    public SyncService(BillRepository billRepository, BillService billService) {
        this.billRepository = billRepository;
        this.billService = billService;
    }

    public String syncBill(Bill bill, Locale locale) {
        // Check if bill exists (idempotency)
        Optional<Bill> existingBill = billRepository.findByBillId(bill.getBillId());
        if (existingBill.isPresent()) {
            System.out.println("[SYNC] Bill already exists: " + bill.getBillId());
            return bill.getBillId();
        }
        // Create new bill
        Bill savedBill = billService.createBill(bill, locale);
        System.out.println("[SYNC] Bill synced: " + savedBill.getBillId());
        return savedBill.getBillId();
    }
}