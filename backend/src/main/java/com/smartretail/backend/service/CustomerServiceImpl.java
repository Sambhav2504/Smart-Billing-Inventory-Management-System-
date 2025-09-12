package com.smartretail.backend.service;

import com.smartretail.backend.models.Customer;
import com.smartretail.backend.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class CustomerServiceImpl implements CustomerService {
    private final CustomerRepository customerRepository;

    public CustomerServiceImpl(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    @Transactional
    public Customer createCustomer(Customer customer) {
        System.out.println("[SERVICE] Creating customer: " + customer.getName());
        if (customer.getMobile() != null && customerRepository.existsByMobile(customer.getMobile())) {
            System.out.println("[SERVICE] Create failed: Mobile already exists: " + customer.getMobile());
            throw new RuntimeException("Mobile already exists");
        }
        customer.setCreatedAt(new Date());
        Customer savedCustomer = customerRepository.save(customer);
        System.out.println("[SERVICE] Customer created successfully: " + savedCustomer.getId());
        return savedCustomer;
    }

    @Override
    public Customer getCustomerByMobile(String mobile) {
        System.out.println("[SERVICE] Fetching customer with mobile: " + mobile);
        Optional<Customer> optionalCustomer = customerRepository.findByMobile(mobile);
        return optionalCustomer.orElse(null);
    }

    @Override
    public List<Customer> getAllCustomers() {
        System.out.println("[SERVICE] Fetching all customers.");
        return customerRepository.findAll();
    }

    @Override
    @Transactional
    public Customer updateCustomer(String id, Customer updateData) {
        System.out.println("[SERVICE] Updating customer ID: " + id);
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> {
                    System.out.println("[SERVICE] Update failed: Customer not found for ID: " + id);
                    return new RuntimeException("Customer not found");
                });
        if (updateData.getName() != null) customer.setName(updateData.getName());
        if (updateData.getEmail() != null) customer.setEmail(updateData.getEmail());
        if (updateData.getMobile() != null) {
            if (customerRepository.existsByMobile(updateData.getMobile()) &&
                    !customer.getMobile().equals(updateData.getMobile())) {
                System.out.println("[SERVICE] Update failed: Mobile already exists: " + updateData.getMobile());
                throw new RuntimeException("Mobile already exists");
            }
            customer.setMobile(updateData.getMobile());
        }
        Customer updatedCustomer = customerRepository.save(customer);
        System.out.println("[SERVICE] Customer updated successfully: " + updatedCustomer.getId());
        return updatedCustomer;
    }

    @Override
    @Transactional
    public void deleteCustomer(String id) {
        System.out.println("[SERVICE] Deleting customer ID: " + id);
        if (!customerRepository.existsById(id)) {
            System.out.println("[SERVICE] Delete failed: Customer not found for ID: " + id);
            throw new RuntimeException("Customer not found");
        }
        customerRepository.deleteById(id);
        System.out.println("[SERVICE] Customer deleted successfully.");
    }

    @Override
    public List<String> getCustomerPurchaseHistory(String customerId) {
        System.out.println("[SERVICE] Fetching purchase history for customer ID: " + customerId);
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        return customer.getPurchaseHistory();
    }
}