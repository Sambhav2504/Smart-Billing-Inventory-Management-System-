package com.smartretail.backend.repository;

import com.smartretail.backend.models.Customer;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface CustomerRepository extends MongoRepository<Customer, String> {
    Optional<Customer> findByMobile(String mobile);
    boolean existsByMobile(String mobile);
}