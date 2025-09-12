package com.smartretail.backend.service;

import com.smartretail.backend.models.Customer;

import java.util.List;

public interface CustomerService {
    Customer createCustomer(Customer customer);
    Customer getCustomerByMobile(String mobile);
    List<Customer> getAllCustomers();
    Customer updateCustomer(String id, Customer updateData);
    void deleteCustomer(String id);
    List<String> getCustomerPurchaseHistory(String customerId);
}