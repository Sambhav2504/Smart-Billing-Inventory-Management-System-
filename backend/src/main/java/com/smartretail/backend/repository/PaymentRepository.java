package com.smartretail.backend.repository;

import com.smartretail.backend.models.Payment;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface PaymentRepository extends MongoRepository<Payment, String> {
    Optional<Payment> findByPaymentId(String paymentId);
    boolean existsByPaymentId(String paymentId);
}