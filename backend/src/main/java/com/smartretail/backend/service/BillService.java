package com.smartretail.backend.service;

import com.smartretail.backend.models.Bill;
import java.util.List;

public interface BillService {
    Bill createBill(Bill bill);
    Bill getBillById(String billId);
    List<Bill> getAllBills();
}