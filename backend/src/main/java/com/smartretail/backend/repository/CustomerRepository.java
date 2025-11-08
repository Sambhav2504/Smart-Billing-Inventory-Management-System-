package com.smartretail.backend.repository;

import com.smartretail.backend.models.Customer;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Date; // **<-- ADD IMPORT**
import java.util.List; // **<-- ADD IMPORT**
import java.util.Optional;

public interface CustomerRepository extends MongoRepository<Customer, String> {
    Optional<Customer> findByMobile(String mobile);
    boolean existsByMobile(String mobile);

    // **--- ADD THIS NEW METHOD ---**
    List<Customer> findByLastPurchaseDateBeforeOrLastPurchaseDateIsNull(Date thresholdDate);
}