package com.smartretail.backend.service;

import com.smartretail.backend.models.Bill;
import com.smartretail.backend.models.Customer;
import com.smartretail.backend.repository.CustomerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class CustomerServiceImpl implements CustomerService {

    private static final Logger logger = LoggerFactory.getLogger(CustomerServiceImpl.class); // ADD THIS
    private final CustomerRepository customerRepository;
    private final MessageSource messageSource;

    public CustomerServiceImpl(CustomerRepository customerRepository, MessageSource messageSource) {
        this.customerRepository = customerRepository;
        this.messageSource = messageSource;
    }

    @Override
    public Customer findOrCreateCustomer(Bill.CustomerInfo customerInfo, Locale locale) {
        logger.debug("[CUSTOMER SERVICE] Finding or creating customer with mobile: {}", customerInfo.getMobile());

        // First, try to find by mobile
        Optional<Customer> existingCustomer = customerRepository.findByMobile(customerInfo.getMobile());

        if (existingCustomer.isPresent()) {
            Customer customer = existingCustomer.get();
            logger.debug("[CUSTOMER SERVICE] Found existing customer: {}", customer.getMobile());
            logger.debug("[CUSTOMER SERVICE] Existing customer email: {}", customer.getEmail());

            // Update customer info if new data is provided
            if (customerInfo.getName() != null && !customerInfo.getName().isEmpty()) {
                customer.setName(customerInfo.getName());
                logger.debug("[CUSTOMER SERVICE] Updated customer name to: {}", customerInfo.getName());
            }
            if (customerInfo.getEmail() != null && !customerInfo.getEmail().isEmpty()) {
                customer.setEmail(customerInfo.getEmail());
                logger.debug("[CUSTOMER SERVICE] Updated customer email to: {}", customerInfo.getEmail());
            }

            Customer savedCustomer = customerRepository.save(customer);
            logger.debug("[CUSTOMER SERVICE] Customer saved with email: {}", savedCustomer.getEmail());
            return savedCustomer;
        } else {
            // Create new customer
            Customer newCustomer = new Customer();
            newCustomer.setMobile(customerInfo.getMobile());
            newCustomer.setName(customerInfo.getName() != null ? customerInfo.getName() : "Customer");
            newCustomer.setEmail(customerInfo.getEmail());
            newCustomer.setCreatedAt(new Date());

            logger.debug("[CUSTOMER SERVICE] Creating new customer: {}", newCustomer.getMobile());
            logger.debug("[CUSTOMER SERVICE] New customer email: {}", newCustomer.getEmail());

            Customer savedCustomer = customerRepository.save(newCustomer);
            logger.debug("[CUSTOMER SERVICE] New customer created with email: {}", savedCustomer.getEmail());
            return savedCustomer;
        }
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

