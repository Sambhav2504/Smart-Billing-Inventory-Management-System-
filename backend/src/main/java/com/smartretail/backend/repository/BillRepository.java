package com.smartretail.backend.repository;

import com.smartretail.backend.models.Bill;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface BillRepository extends MongoRepository<Bill, String> {
    Optional<Bill> findByBillId(String billId);
    boolean existsByBillId(String billId);

}
