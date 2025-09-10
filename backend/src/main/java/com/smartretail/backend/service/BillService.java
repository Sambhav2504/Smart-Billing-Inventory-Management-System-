package com.smartretail.backend.service;

import com.smartretail.backend.models.Bill;

public interface BillService {
    Bill createBill(Bill bill);
    Bill getBillById(String billId);
}