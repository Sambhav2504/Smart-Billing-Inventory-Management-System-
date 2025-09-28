package com.smartretail.backend.controller;

import com.smartretail.backend.models.Bill;
import com.smartretail.backend.models.Customer;
import com.smartretail.backend.service.CustomerService;
import com.smartretail.backend.service.ReminderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerControllerTest {

    @Mock
    private CustomerService customerService;

    @Mock
    private ReminderService reminderService;

    @InjectMocks
    private CustomerController customerController;

    @Test
    void testCreateCustomer_Success() {
        Customer customer = new Customer();
        customer.setId("68d7eb12a2d18777fbd48685");
        customer.setMobile("5554447788");
        customer.setName("Sita");
        when(customerService.createCustomer(any(Customer.class), eq(Locale.ENGLISH))).thenReturn(customer);

        ResponseEntity<Customer> response = customerController.createCustomer(customer, Locale.ENGLISH);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("68d7eb12a2d18777fbd48685", response.getBody().getId());
        verify(customerService).createCustomer(customer, Locale.ENGLISH);
    }

    @Test
    void testGetCustomerById_Success() {
        Customer customer = new Customer();
        customer.setId("68d7eb12a2d18777fbd48685");
        customer.setMobile("5554447788");
        when(customerService.getCustomerById("68d7eb12a2d18777fbd48685", Locale.ENGLISH)).thenReturn(customer);

        ResponseEntity<Customer> response = customerController.getCustomerById("68d7eb12a2d18777fbd48685", Locale.ENGLISH);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("68d7eb12a2d18777fbd48685", response.getBody().getId());
        verify(customerService).getCustomerById("68d7eb12a2d18777fbd48685", Locale.ENGLISH);
    }

    @Test
    void testGetAllCustomers_Success() {
        Customer customer = new Customer();
        customer.setId("68d7eb12a2d18777fbd48685");
        when(customerService.getAllCustomers()).thenReturn(Arrays.asList(customer));

        ResponseEntity<List<Customer>> response = customerController.getAllCustomers(Locale.ENGLISH);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        verify(customerService).getAllCustomers();
    }

    @Test
    void testUpdateCustomer_Success() {
        Customer customer = new Customer();
        customer.setId("68d7eb12a2d18777fbd48685");
        customer.setMobile("5554447788");
        when(customerService.updateCustomer(eq("68d7eb12a2d18777fbd48685"), any(Customer.class), eq(Locale.ENGLISH))).thenReturn(customer);

        ResponseEntity<Customer> response = customerController.updateCustomer("68d7eb12a2d18777fbd48685", customer, Locale.ENGLISH);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("68d7eb12a2d18777fbd48685", response.getBody().getId());
        verify(customerService).updateCustomer("68d7eb12a2d18777fbd48685", customer, Locale.ENGLISH);
    }

    @Test
    void testDeleteCustomer_Success() {
        doNothing().when(customerService).deleteCustomer("68d7eb12a2d18777fbd48685", Locale.ENGLISH);

        ResponseEntity<Void> response = customerController.deleteCustomer("68d7eb12a2d18777fbd48685", Locale.ENGLISH);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(customerService).deleteCustomer("68d7eb12a2d18777fbd48685", Locale.ENGLISH);
    }

    @Test
    void testGetCustomerPurchaseHistory_Success() {
        Bill bill1 = new Bill();
        bill1.setBillId("bill001");
        bill1.setTotalAmount(500.0);
        bill1.setCreatedAt(new Date(2025 - 1900, 8, 27, 10, 0, 0));
        Bill.CustomerInfo billCustomerInfo1 = new Bill.CustomerInfo();
        billCustomerInfo1.setMobile("5554447788");
        bill1.setCustomer(billCustomerInfo1);

        Bill bill2 = new Bill();
        bill2.setBillId("bill002");
        bill2.setTotalAmount(300.0);
        bill2.setCreatedAt(new Date(2025 - 1900, 8, 27, 12, 0, 0));
        Bill.CustomerInfo billCustomerInfo2 = new Bill.CustomerInfo();
        billCustomerInfo2.setMobile("5554447788");
        bill2.setCustomer(billCustomerInfo2);

        Bill bill3 = new Bill();
        bill3.setBillId("bill003");
        bill3.setTotalAmount(200.0);
        bill3.setCreatedAt(new Date(2025 - 1900, 8, 27, 15, 0, 0));
        Bill.CustomerInfo billCustomerInfo3 = new Bill.CustomerInfo();
        billCustomerInfo3.setMobile("5554447788");
        bill3.setCustomer(billCustomerInfo3);

        when(customerService.getCustomerPurchaseHistory("68d7eb12a2d18777fbd48685", null, null, Locale.ENGLISH))
                .thenReturn(Arrays.asList(bill1, bill2, bill3));

        ResponseEntity<List<Bill>> response = customerController.getCustomerPurchaseHistory("68d7eb12a2d18777fbd48685", null, null, Locale.ENGLISH);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(3, response.getBody().size());
        assertEquals("bill001", response.getBody().get(0).getBillId());
        assertEquals("bill002", response.getBody().get(1).getBillId());
        assertEquals("bill003", response.getBody().get(2).getBillId());
        verify(customerService).getCustomerPurchaseHistory("68d7eb12a2d18777fbd48685", null, null, Locale.ENGLISH);
    }

    @Test
    void testGetCustomerPurchaseHistory_BillNotFound() {
        when(customerService.getCustomerPurchaseHistory("68d7eb12a2d18777fbd48685", null, null, Locale.ENGLISH))
                .thenReturn(List.of());

        ResponseEntity<List<Bill>> response = customerController.getCustomerPurchaseHistory("68d7eb12a2d18777fbd48685", null, null, Locale.ENGLISH);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
        verify(customerService).getCustomerPurchaseHistory("68d7eb12a2d18777fbd48685", null, null, Locale.ENGLISH);
    }

    @Test
    void testGetCustomerPurchaseHistory_WithDateRange() {
        Date startDate = new Date(2025 - 1900, 8, 27, 0, 0, 0); // 2025-09-27 00:00:00
        Date endDate = new Date(2025 - 1900, 8, 27, 23, 59, 59); // 2025-09-27 23:59:59
        Bill bill1 = new Bill();
        bill1.setBillId("bill001");
        bill1.setTotalAmount(500.0);
        bill1.setCreatedAt(new Date(2025 - 1900, 8, 27, 10, 0, 0));
        Bill.CustomerInfo billCustomerInfo1 = new Bill.CustomerInfo();
        billCustomerInfo1.setMobile("5554447788");
        bill1.setCustomer(billCustomerInfo1);

        Bill bill2 = new Bill();
        bill2.setBillId("bill002");
        bill2.setTotalAmount(300.0);
        bill2.setCreatedAt(new Date(2025 - 1900, 8, 27, 12, 0, 0));
        Bill.CustomerInfo billCustomerInfo2 = new Bill.CustomerInfo();
        billCustomerInfo2.setMobile("5554447788");
        bill2.setCustomer(billCustomerInfo2);

        Bill bill3 = new Bill();
        bill3.setBillId("bill003");
        bill3.setTotalAmount(200.0);
        bill3.setCreatedAt(new Date(2025 - 1900, 8, 27, 15, 0, 0));
        Bill.CustomerInfo billCustomerInfo3 = new Bill.CustomerInfo();
        billCustomerInfo3.setMobile("5554447788");
        bill3.setCustomer(billCustomerInfo3);

        when(customerService.getCustomerPurchaseHistory("68d7eb12a2d18777fbd48685", startDate, endDate, Locale.ENGLISH))
                .thenReturn(Arrays.asList(bill1, bill2, bill3));

        ResponseEntity<List<Bill>> response = customerController.getCustomerPurchaseHistory("68d7eb12a2d18777fbd48685", startDate, endDate, Locale.ENGLISH);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(3, response.getBody().size());
        assertEquals("bill001", response.getBody().get(0).getBillId());
        assertEquals("bill002", response.getBody().get(1).getBillId());
        assertEquals("bill003", response.getBody().get(2).getBillId());
        verify(customerService).getCustomerPurchaseHistory("68d7eb12a2d18777fbd48685", startDate, endDate, Locale.ENGLISH);
    }

    @Test
    void testGetCustomerPurchaseHistory_CustomerNotFound() {
        when(customerService.getCustomerPurchaseHistory("68d7eb12a2d18777fbd48685", null, null, Locale.ENGLISH))
                .thenThrow(new IllegalArgumentException("Customer not found: 68d7eb12a2d18777fbd48685"));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> customerController.getCustomerPurchaseHistory("68d7eb12a2d18777fbd48685", null, null, Locale.ENGLISH));
        assertEquals("Customer not found: 68d7eb12a2d18777fbd48685", exception.getMessage());
        verify(customerService).getCustomerPurchaseHistory("68d7eb12a2d18777fbd48685", null, null, Locale.ENGLISH);
    }

    @Test
    void testTriggerMonthlyPurchaseReminders_Success() {
        doNothing().when(reminderService).sendMonthlyPurchaseReminders();

        ResponseEntity<Map<String, String>> response = customerController.triggerMonthlyPurchaseReminders(Locale.ENGLISH);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Monthly purchase reminders triggered successfully", response.getBody().get("message"));
        verify(reminderService).sendMonthlyPurchaseReminders();
    }
}