package com.smartretail.backend.repository;

import com.smartretail.backend.models.Bill;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface BillRepository extends MongoRepository<Bill, String> {
    Bill findByBillId(String billId);
    boolean existsByBillId(String billId);
}