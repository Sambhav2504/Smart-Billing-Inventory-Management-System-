package com.smartretail.backend.service;

import com.smartretail.backend.models.Bill;
import com.smartretail.backend.models.Customer;
import com.smartretail.backend.models.Product;
import com.smartretail.backend.repository.BillRepository;
import com.smartretail.backend.repository.CustomerRepository;
import com.smartretail.backend.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BillServiceTest {

    @Mock
    private BillRepository billRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private BillServiceImpl billService;

    private Bill bill;
    private Customer customer;
    private Product product;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setMobile("9876543210");
        customer.setName("Sita");

        product = new Product();
        product.setProductId("p12345678");
        product.setPrice(999.99);
        product.setQuantity(10);

        Bill.BillItem item = new Bill.BillItem();
        item.setProductId("p12345678");
        item.setQty(2);
        item.setPrice(999.99);

        Bill.CustomerInfo customerInfo = new Bill.CustomerInfo();
        customerInfo.setMobile("9876543210");

        bill = new Bill();
        bill.setItems(Arrays.asList(item));
        bill.setCustomer(customerInfo);
    }

    @Test
    void testCreateBillSuccess() {
        when(customerRepository.findByMobile("9876543210")).thenReturn(Optional.of(customer));
        when(productRepository.findByProductId("p12345678")).thenReturn(Optional.of(product));
        when(billRepository.existsByBillId(anyString())).thenReturn(false);
        when(billRepository.save(any(Bill.class))).thenReturn(bill);

        Bill result = billService.createBill(bill);

        assertNotNull(result);
        assertEquals(1999.98, result.getTotal());
        assertNotNull(result.getBillId());
        assertNotNull(result.getCreatedAt());
        verify(productRepository).save(product);
        assertEquals(8, product.getQuantity());
    }

    @Test
    void testCreateBillCustomerNotFound() {
        when(customerRepository.findByMobile("9876543210")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> billService.createBill(bill));
        assertEquals("Customer not found: 9876543210", exception.getMessage());
    }

    @Test
    void testCreateBillInsufficientStock() {
        when(customerRepository.findByMobile("9876543210")).thenReturn(Optional.of(customer));
        when(productRepository.findByProductId("p12345678")).thenReturn(Optional.of(product));
        bill.getItems().get(0).setQty(20);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> billService.createBill(bill));
        assertEquals("Insufficient stock for product: p12345678", exception.getMessage());
    }

    @Test
    void testGetBillByIdSuccess() {
        when(billRepository.findByBillId("b12345678")).thenReturn(Optional.of(bill));

        Bill result = billService.getBillById("b12345678");

        assertNotNull(result);
        assertEquals("9876543210", result.getCustomer().getMobile());
    }

    @Test
    void testGetBillByIdNotFound() {
        when(billRepository.findByBillId("b12345678")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> billService.getBillById("b12345678"));
        assertEquals("Bill not found: b12345678", exception.getMessage());
    }


    @Test
    void testGetAllBills() {
        when(billRepository.findAll()).thenReturn(Arrays.asList(bill));

        List<Bill> bills = billService.getAllBills();

        assertEquals(1, bills.size());
        assertEquals(1, bills.get(0).getItems().size());
        assertEquals("9876543210", bills.get(0).getCustomer().getMobile());
    }
}