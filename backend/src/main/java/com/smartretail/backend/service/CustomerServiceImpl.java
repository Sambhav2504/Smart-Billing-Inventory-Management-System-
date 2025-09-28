package com.smartretail.backend.service;

import com.smartretail.backend.models.Bill;
import com.smartretail.backend.models.Customer;
import com.smartretail.backend.repository.BillRepository;
import com.smartretail.backend.repository.CustomerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CustomerServiceImpl implements CustomerService {
    private static final Logger logger = LoggerFactory.getLogger(CustomerServiceImpl.class);
    private final CustomerRepository customerRepository;
    private final BillRepository billRepository;
    private final MessageSource messageSource;

    public CustomerServiceImpl(CustomerRepository customerRepository, BillRepository billRepository, MessageSource messageSource) {
        this.customerRepository = customerRepository;
        this.billRepository = billRepository;
        this.messageSource = messageSource;
    }

    @Override
    public Customer findOrCreateCustomer(Bill.CustomerInfo customerInfo, Locale locale) {
        logger.debug("[CUSTOMER SERVICE] Finding or creating customer with mobile: {}", customerInfo.getMobile());

        if (customerInfo.getMobile() == null || customerInfo.getMobile().isEmpty()) {
            logger.error("[CUSTOMER SERVICE] Customer mobile is required");
            throw new IllegalArgumentException(messageSource.getMessage("customer.mobile.required", null, locale));
        }

        Optional<Customer> existingCustomer = customerRepository.findByMobile(customerInfo.getMobile());
        if (existingCustomer.isPresent()) {
            Customer customer = existingCustomer.get();
            logger.debug("[CUSTOMER SERVICE] Found existing customer: {}", customer.getMobile());
            logger.debug("[CUSTOMER SERVICE] Existing customer email: {}", customer.getEmail());

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
            Customer newCustomer = new Customer();
            newCustomer.setMobile(customerInfo.getMobile());
            newCustomer.setName(customerInfo.getName() != null ? customerInfo.getName() : "Customer");
            newCustomer.setEmail(customerInfo.getEmail());
            newCustomer.setCreatedAt(new Date());
            newCustomer.setPurchaseHistory(new ArrayList<>());

            logger.debug("[CUSTOMER SERVICE] Creating new customer: {}", newCustomer.getMobile());
            logger.debug("[CUSTOMER SERVICE] New customer email: {}", newCustomer.getEmail());

            Customer savedCustomer = customerRepository.save(newCustomer);
            logger.debug("[CUSTOMER SERVICE] New customer created with email: {}", savedCustomer.getEmail());
            return savedCustomer;
        }
    }

    @Override
    public void addBillId(String mobile, String billId) {
        logger.debug("[CUSTOMER SERVICE] Adding bill ID {} to customer with mobile: {}", billId, mobile);
        Customer customer = customerRepository.findByMobile(mobile)
                .orElseThrow(() -> new IllegalArgumentException(
                        messageSource.getMessage("customer.not.found", new Object[]{mobile}, Locale.ENGLISH)));
        if (customer.getPurchaseHistory() == null) {
            customer.setPurchaseHistory(new ArrayList<>());
        }
        if (!customer.getPurchaseHistory().contains(billId)) {
            customer.getPurchaseHistory().add(billId);
            customerRepository.save(customer);
            logger.info("[CUSTOMER SERVICE] Bill ID {} added to customer: {}", billId, mobile);
        }
    }

    @Override
    public Customer createCustomer(Customer customer, Locale locale) {
        logger.debug("[CUSTOMER SERVICE] Creating customer with mobile: {}", customer.getMobile());
        if (customerRepository.findByMobile(customer.getMobile()).isPresent()) {
            logger.error("[CUSTOMER SERVICE] Customer already exists: {}", customer.getMobile());
            throw new IllegalArgumentException(
                    messageSource.getMessage("customer.exists", new Object[]{customer.getMobile()}, locale));
        }
        customer.setCreatedAt(new Date());
        Customer savedCustomer = customerRepository.save(customer);
        logger.info("[CUSTOMER SERVICE] Customer created: {}", savedCustomer.getMobile());
        return savedCustomer;
    }

    @Override
    public Customer getCustomerById(String id, Locale locale) {
        logger.debug("[CUSTOMER SERVICE] Fetching customer with ID: {}", id);
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        messageSource.getMessage("customer.not.found", new Object[]{id}, locale)));
        logger.info("[CUSTOMER SERVICE] Found customer: {}", customer.getMobile());
        return customer;
    }

    @Override
    public List<Customer> getAllCustomers() {
        logger.debug("[CUSTOMER SERVICE] Fetching all customers");
        List<Customer> customers = customerRepository.findAll();
        logger.info("[CUSTOMER SERVICE] Found {} customers", customers.size());
        return customers;
    }

    @Override
    public Customer updateCustomer(String id, Customer customer, Locale locale) {
        logger.debug("[CUSTOMER SERVICE] Updating customer with ID: {}", id);
        Customer existing = customerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        messageSource.getMessage("customer.not.found", new Object[]{id}, locale)));
        existing.setName(customer.getName());
        existing.setEmail(customer.getEmail());
        existing.setMobile(customer.getMobile());
        Customer updatedCustomer = customerRepository.save(existing);
        logger.info("[CUSTOMER SERVICE] Customer updated: {}", updatedCustomer.getMobile());
        return updatedCustomer;
    }

    @Override
    public void deleteCustomer(String id, Locale locale) {
        logger.debug("[CUSTOMER SERVICE] Deleting customer with ID: {}", id);
        if (!customerRepository.existsById(id)) {
            logger.error("[CUSTOMER SERVICE] Customer not found: {}", id);
            throw new IllegalArgumentException(
                    messageSource.getMessage("customer.not.found", new Object[]{id}, locale));
        }
        customerRepository.deleteById(id);
        logger.info("[CUSTOMER SERVICE] Customer deleted: {}", id);
    }

    @Override
    public List<Bill> getCustomerPurchaseHistory(String customerId, Date startDate, Date endDate, Locale locale) {
        logger.debug("[CUSTOMER SERVICE] Fetching purchase history for customer ID: {}, startDate: {}, endDate: {}",
                customerId, startDate, endDate);

        if (customerId == null || customerId.isEmpty()) {
            logger.error("[CUSTOMER SERVICE] Customer ID is required");
            throw new IllegalArgumentException(messageSource.getMessage("customer.id.required", null, locale));
        }

        if (startDate != null && endDate != null && startDate.after(endDate)) {
            logger.error("[CUSTOMER SERVICE] Invalid date range: startDate={} is after endDate={}", startDate, endDate);
            throw new IllegalArgumentException(
                    messageSource.getMessage("report.date.range.invalid", new Object[]{startDate, endDate}, locale));
        }

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException(
                        messageSource.getMessage("customer.not.found", new Object[]{customerId}, locale)));

        List<String> billIds = customer.getPurchaseHistory();
        if (billIds == null || billIds.isEmpty()) {
            logger.info("[CUSTOMER SERVICE] No purchase history found for customer: {}", customerId);
            return new ArrayList<>();
        }

        List<Bill> bills = billRepository.findAllById(billIds).stream()
                .filter(bill -> bill.getCreatedAt() != null)
                .filter(bill -> startDate == null || !bill.getCreatedAt().before(startDate))
                .filter(bill -> endDate == null || !bill.getCreatedAt().after(endDate))
                .collect(Collectors.toList());

        if (bills.size() < billIds.size()) {
            List<String> foundBillIds = bills.stream().map(Bill::getBillId).collect(Collectors.toList());
            List<String> missingBillIds = new ArrayList<>(billIds);
            missingBillIds.removeAll(foundBillIds);
            logger.warn("[CUSTOMER SERVICE] Some bill IDs not found: missing IDs {}, expected {}, found {}",
                    missingBillIds, billIds.size(), bills.size());
        }

        logger.info("[CUSTOMER SERVICE] Found {} bills in purchase history for customer: {}", bills.size(), customerId);
        return bills;
    }
}
