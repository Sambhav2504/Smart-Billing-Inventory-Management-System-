
package com.smartretail.backend.service;

import com.smartretail.backend.models.Bill;
import com.smartretail.backend.models.Customer;

import java.util.Date;
import java.util.List;
import java.util.Locale;

public interface CustomerService {
    Customer findOrCreateCustomer(Bill.CustomerInfo customerInfo, Locale locale);
    void addBillId(String mobile, String billId);
    Customer createCustomer(Customer customer, Locale locale);
    Customer getCustomerById(String id, Locale locale);
    List<Customer> getAllCustomers();
    Customer updateCustomer(String id, Customer customer, Locale locale);
    void deleteCustomer(String id, Locale locale);
    List<Bill> getCustomerPurchaseHistory(String customerId, Date startDate, Date endDate, Locale locale);
}
