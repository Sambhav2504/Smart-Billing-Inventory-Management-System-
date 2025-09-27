package com.smartretail.backend.service;

import com.smartretail.backend.models.Bill;
import com.smartretail.backend.models.Customer;
import com.smartretail.backend.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private CustomerServiceImpl customerService;

    private Customer customer;
    private Bill.CustomerInfo customerInfo;
    private Locale locale;

    @BeforeEach
    void setUp() {
        locale = Locale.forLanguageTag("en");

        customer = new Customer();
        customer.setId("c123");
        customer.setName("Sita");
        customer.setEmail("sita@gmail.com");
        customer.setMobile("+919876543210");
        customer.setCreatedAt(new Date());
        customer.setPurchaseHistory(Arrays.asList("b123", "b124"));

        customerInfo = new Bill.CustomerInfo();
        customerInfo.setName("Sita");
        customerInfo.setEmail("sita@gmail.com");
        customerInfo.setMobile("+919876543210");

        when(messageSource.getMessage(eq("customer.exists"), any(), eq(locale)))
                .thenReturn("Customer already exists: +919876543210");
        when(messageSource.getMessage(eq("customer.not.found"), any(), eq(locale)))
                .thenReturn("Customer not found: +919876543210");
    }

    @Test
    void testCreateCustomerSuccess() {
        when(customerRepository.findByMobile("+919876543210")).thenReturn(Optional.empty());
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);

        Customer result = customerService.createCustomer(customer, locale);

        assertNotNull(result);
        assertEquals("Sita", result.getName());
        verify(customerRepository).save(customer);
    }

    @Test
    void testCreateCustomerDuplicateMobile() {
        when(customerRepository.findByMobile("+919876543210")).thenReturn(Optional.of(customer));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> customerService.createCustomer(customer, locale));
        assertEquals("Customer already exists: +919876543210", exception.getMessage());
    }

    @Test
    void testFindOrCreateCustomerExisting() {
        when(customerRepository.findByMobile("+919876543210")).thenReturn(Optional.of(customer));

        Customer result = customerService.findOrCreateCustomer(customerInfo, locale);

        assertNotNull(result);
        assertEquals("Sita", result.getName());
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void testFindOrCreateCustomerNew() {
        when(customerRepository.findByMobile("+919876543210")).thenReturn(Optional.empty());
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);

        Customer result = customerService.findOrCreateCustomer(customerInfo, locale);

        assertNotNull(result);
        assertEquals("Sita", result.getName());
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    void testAddBillId() {
        when(customerRepository.findByMobile("+919876543210")).thenReturn(Optional.of(customer));
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);

        customerService.addBillId("+919876543210", "b125");

        assertTrue(customer.getPurchaseHistory().contains("b125"));
        verify(customerRepository).save(customer);
    }

    @Test
    void testAddBillIdCustomerNotFound() {
        when(customerRepository.findByMobile("+919876543210")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> customerService.addBillId("+919876543210", "b125"));
        assertEquals("Customer not found: +919876543210", exception.getMessage());
    }

    @Test
    void testGetCustomerByIdSuccess() {
        when(customerRepository.findById("c123")).thenReturn(Optional.of(customer));

        Customer result = customerService.getCustomerById("c123", locale);

        assertNotNull(result);
        assertEquals("Sita", result.getName());
    }

    @Test
    void testGetCustomerByIdNotFound() {
        when(customerRepository.findById("c123")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> customerService.getCustomerById("c123", locale));
        assertEquals("Customer not found: c123", exception.getMessage());
    }

    @Test
    void testGetAllCustomers() {
        when(customerRepository.findAll()).thenReturn(Arrays.asList(customer));

        List<Customer> customers = customerService.getAllCustomers();

        assertEquals(1, customers.size());
        assertEquals("Sita", customers.get(0).getName());
    }

    @Test
    void testUpdateCustomerSuccess() {
        Customer updatedData = new Customer();
        updatedData.setName("Sita Updated");
        updatedData.setEmail("sita.updated@gmail.com");
        updatedData.setMobile("+919876543211");

        when(customerRepository.findById("c123")).thenReturn(Optional.of(customer));
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);

        Customer result = customerService.updateCustomer("c123", updatedData, locale);

        assertEquals("Sita Updated", customer.getName());
        assertEquals("sita.updated@gmail.com", customer.getEmail());
        assertEquals("+919876543211", customer.getMobile());
        verify(customerRepository).save(customer);
    }

    @Test
    void testUpdateCustomerNotFound() {
        when(customerRepository.findById("c123")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> customerService.updateCustomer("c123", customer, locale));
        assertEquals("Customer not found: c123", exception.getMessage());
    }

    @Test
    void testDeleteCustomerSuccess() {
        when(customerRepository.existsById("c123")).thenReturn(true);

        customerService.deleteCustomer("c123", locale);

        verify(customerRepository).deleteById("c123");
    }

    @Test
    void testDeleteCustomerNotFound() {
        when(customerRepository.existsById("c123")).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> customerService.deleteCustomer("c123", locale));
        assertEquals("Customer not found: c123", exception.getMessage());
    }

    @Test
    void testGetCustomerPurchaseHistory() {
        when(customerRepository.findById("c123")).thenReturn(Optional.of(customer));

        List<String> purchaseHistory = customerService.getCustomerPurchaseHistory("c123");

        assertEquals(Arrays.asList("b123", "b124"), purchaseHistory);
    }

    @Test
    void testGetCustomerPurchaseHistoryNotFound() {
        when(customerRepository.findById("c123")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> customerService.getCustomerPurchaseHistory("c123"));
        assertEquals("Customer not found", exception.getMessage());
    }
}