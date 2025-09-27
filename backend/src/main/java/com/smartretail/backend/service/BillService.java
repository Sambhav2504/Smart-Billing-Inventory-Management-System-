package com.smartretail.backend.service;

import com.smartretail.backend.models.Bill;

import java.util.List;
import java.util.Locale;

public interface BillService {
    Bill createBill(Bill bill, Locale locale);
    Bill getBillById(String billId, Locale locale);
    List<Bill> getAllBills();
    boolean validatePdfAccessToken(String billId, String token);
    boolean existsByBillId(String billId);
}