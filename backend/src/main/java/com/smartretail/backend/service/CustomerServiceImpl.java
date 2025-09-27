package com.smartretail.backend.service;

import com.smartretail.backend.models.Bill;
import com.smartretail.backend.models.Customer;
import com.smartretail.backend.repository.CustomerRepository;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Locale;

@Service
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final MessageSource messageSource;

    public CustomerServiceImpl(CustomerRepository customerRepository, MessageSource messageSource) {
        this.customerRepository = customerRepository;
        this.messageSource = messageSource;
    }

    @Override
    public Customer findOrCreateCustomer(Bill.CustomerInfo customerInfo, Locale locale) {
        Customer customer = customerRepository.findByMobile(customerInfo.getMobile())
                .orElseGet(() -> {
                    Customer newCustomer = new Customer();
                    newCustomer.setName(customerInfo.getName());
                    newCustomer.setEmail(customerInfo.getEmail());
                    newCustomer.setMobile(customerInfo.getMobile());
                    newCustomer.setCreatedAt(new Date());
                    return customerRepository.save(newCustomer);
                });
        return customer;
    }

    @Override
    public void addBillId(String mobile, String billId) {
        Customer customer = customerRepository.findByMobile(mobile)
                .orElseThrow(() -> new RuntimeException(
                        messageSource.getMessage("customer.not.found", new Object[]{mobile}, Locale.getDefault())));
        customer.addBillId(billId);
        customerRepository.save(customer);
    }

    @Override
    public Customer createCustomer(Customer customer, Locale locale) {
        if (customerRepository.findByMobile(customer.getMobile()).isPresent()) {
            throw new RuntimeException(
                    messageSource.getMessage("customer.exists", new Object[]{customer.getMobile()}, locale));
        }
        customer.setCreatedAt(new Date());
        return customerRepository.save(customer);
    }

    @Override
    public Customer getCustomerById(String id, Locale locale) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        messageSource.getMessage("customer.not.found", new Object[]{id}, locale)));
    }

    @Override
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    @Override
    public Customer updateCustomer(String id, Customer customer, Locale locale) {
        Customer existing = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        messageSource.getMessage("customer.not.found", new Object[]{id}, locale)));
        existing.setName(customer.getName());
        existing.setEmail(customer.getEmail());
        existing.setMobile(customer.getMobile());
        return customerRepository.save(existing);
    }

    @Override
    public void deleteCustomer(String id, Locale locale) {
        if (!customerRepository.existsById(id)) {
            throw new RuntimeException(
                    messageSource.getMessage("customer.not.found", new Object[]{id}, locale));
        }
        customerRepository.deleteById(id);
    }
    @Override
    public List<String> getCustomerPurchaseHistory(String customerId) {
        System.out.println("[SERVICE] Fetching purchase history for customer ID: " + customerId);
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        return customer.getPurchaseHistory();
    }
}

