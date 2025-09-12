package com.smartretail.backend.service;

import com.smartretail.backend.models.Bill;
import com.smartretail.backend.models.Customer;
import com.smartretail.backend.models.Product;
import com.smartretail.backend.repository.BillRepository;
import com.smartretail.backend.repository.CustomerRepository;
import com.smartretail.backend.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class BillServiceImpl implements BillService {
    private final BillRepository billRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final NotificationService notificationService;

    public BillServiceImpl(BillRepository billRepository, ProductRepository productRepository, CustomerRepository customerRepository, NotificationService notificationService) {
        this.billRepository = billRepository;
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional
    public Bill createBill(Bill bill) {
        System.out.println("[SERVICE] Creating bill for customer: " + bill.getCustomer().getMobile());

        // Generate unique billId
        String billId = "b" + UUID.randomUUID().toString().substring(0, 8);
        if (billRepository.existsByBillId(billId)) {
            throw new RuntimeException("Bill ID already exists");
        }
        bill.setBillId(billId);

        // Validate customer
        Customer customer = customerRepository.findByMobile(bill.getCustomer().getMobile())
                .orElseThrow(() -> new RuntimeException("Customer not found: " + bill.getCustomer().getMobile()));

        // Validate and update stock, calculate total
        double total = 0.0;
        for (Bill.BillItem item : bill.getItems()) {
            Product product = productRepository.findByProductId(item.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + item.getProductId()));
            if (product.getQuantity() < item.getQty()) {
                throw new RuntimeException("Insufficient stock for product: " + item.getProductId());
            }
            // Update stock
            product.setQuantity(product.getQuantity() - item.getQty());
            product.setLastUpdated(new Date());
            productRepository.save(product);
            // Calculate item total
            total += item.getQty() * item.getPrice();
        }
        bill.setTotal(total);
        bill.setCreatedAt(new Date());

        Bill savedBill = billRepository.save(bill);
        System.out.println("[SERVICE] Bill created successfully. ID: " + savedBill.getBillId());

        // Link bill to customer's purchase history
        customer.addBillId(savedBill.getBillId());
        customerRepository.save(customer);

        // Send email notification
        notificationService.sendBillNotification(
                customer.getEmail(),
                savedBill.getBillId(),
                savedBill.getTotal()
        );

        return savedBill;
    }

    @Override
    public Bill getBillById(String billId) {
        System.out.println("[SERVICE] Fetching bill with ID: " + billId);
        return billRepository.findByBillId(billId)
                .orElseThrow(() -> {
                    System.out.println("[SERVICE] Bill not found for ID: " + billId);
                    return new RuntimeException("Bill not found: " + billId);
                });
    }

    @Override
    public List<Bill> getAllBills() {
        System.out.println("[SERVICE] Fetching all bills");
        return billRepository.findAll();
    }
}