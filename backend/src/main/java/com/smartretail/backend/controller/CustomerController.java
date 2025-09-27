package com.smartretail.backend.controller;

import com.smartretail.backend.models.Customer;
import com.smartretail.backend.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {
    private final CustomerService customerService;

    @Autowired
    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER', 'CASHIER')")
    public ResponseEntity<Customer> createCustomer(
            @Valid @RequestBody Customer customer,
            @RequestHeader(value = "Accept-Language", defaultValue = "en") Locale locale) {
        Customer savedCustomer = customerService.createCustomer(customer, locale);
        return ResponseEntity.status(201).body(savedCustomer);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Customer> getCustomerById(
            @PathVariable String id,
            @RequestHeader(value = "Accept-Language", defaultValue = "en") Locale locale) {
        Customer customer = customerService.getCustomerById(id, locale);
        return ResponseEntity.ok(customer);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER', 'CASHIER')")
    public ResponseEntity<List<Customer>> getAllCustomers(
            @RequestHeader(value = "Accept-Language", defaultValue = "en") Locale locale) {
        List<Customer> customers = customerService.getAllCustomers();
        return ResponseEntity.ok(customers);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER')")
    public ResponseEntity<Customer> updateCustomer(
            @PathVariable String id,
            @Valid @RequestBody Customer customer,
            @RequestHeader(value = "Accept-Language", defaultValue = "en") Locale locale) {
        Customer updatedCustomer = customerService.updateCustomer(id, customer, locale);
        return ResponseEntity.ok(updatedCustomer);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER')")
    public ResponseEntity<Void> deleteCustomer(
            @PathVariable String id,
            @RequestHeader(value = "Accept-Language", defaultValue = "en") Locale locale) {
        customerService.deleteCustomer(id, locale);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/purchase-history")
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER', 'CASHIER')")
    public ResponseEntity<List<String>> getCustomerPurchaseHistory(
            @PathVariable String id,
            @RequestHeader(value = "Accept-Language", defaultValue = "en") Locale locale) {
        List<String> purchaseHistory = customerService.getCustomerPurchaseHistory(id);
        return ResponseEntity.ok(purchaseHistory);
    }
}