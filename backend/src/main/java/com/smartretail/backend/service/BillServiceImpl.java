package com.smartretail.backend.service;

import com.smartretail.backend.models.Bill;
import com.smartretail.backend.repository.BillRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class BillServiceImpl implements BillService {
    private final BillRepository billRepository;

    public BillServiceImpl(BillRepository billRepository) {
        this.billRepository = billRepository;
    }

    @Override
    public Bill createBill(Bill bill) {
        System.out.println("[SERVICE] Creating bill: " + bill.getBillId());
        String billId = "inv_" + UUID.randomUUID().toString().substring(0, 8);
        bill.setBillId(billId);
        if (billRepository.existsByBillId(billId)) {
            System.out.println("[SERVICE] Create failed: Bill ID already exists: " + billId);
            throw new RuntimeException("Bill ID already exists");
        }
        bill.setDate(new Date());
        bill.setCashierId(SecurityContextHolder.getContext().getAuthentication().getName());
        Bill savedBill = billRepository.save(bill);
        System.out.println("[SERVICE] Bill created successfully: " + savedBill.getBillId());
        return savedBill;
    }

    @Override
    public Bill getBillById(String billId) {
        System.out.println("[SERVICE] Fetching bill with ID: " + billId);
        return billRepository.findByBillId(billId).orElse(null);
    }

    @Override
    public List<Bill> getAllBills() {
        System.out.println("[SERVICE] Fetching all bills.");
        return billRepository.findAll();
    }
}