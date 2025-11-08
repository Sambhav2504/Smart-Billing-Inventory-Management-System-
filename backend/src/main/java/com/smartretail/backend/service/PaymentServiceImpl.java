package com.smartretail.backend.service;

import com.smartretail.backend.models.Payment;
import com.smartretail.backend.repository.PaymentRepository;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class PaymentServiceImpl implements PaymentService {
    private final PaymentRepository paymentRepository;

    public PaymentServiceImpl(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Override
    public Payment createPayment(Payment payment) {
        System.out.println("[SERVICE] Creating payment: " + payment.getPaymentId());
        String paymentId = "pay_" + UUID.randomUUID().toString().substring(0, 8);
        payment.setPaymentId(paymentId);
        if (paymentRepository.existsByPaymentId(paymentId)) {
            System.out.println("[SERVICE] Create failed: Payment ID already exists: " + paymentId);
            throw new RuntimeException("Payment ID already exists");
        }
        payment.setCreatedAt(new Date());
        Payment savedPayment = paymentRepository.save(payment);
        System.out.println("[SERVICE] Payment created successfully: " + savedPayment.getPaymentId());
        return savedPayment;
    }

    @Override
    public Payment getPaymentById(String paymentId) {
        System.out.println("[SERVICE] Fetching payment with ID: " + paymentId);
        return paymentRepository.findByPaymentId(paymentId).orElse(null);
    }

    @Override
    public List<Payment> getAllPayments() {
        System.out.println("[SERVICE] Fetching all payments.");
        return paymentRepository.findAll();
    }
}