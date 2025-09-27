package com.smartretail.backend.service;

import com.smartretail.backend.models.Bill;
import com.smartretail.backend.models.Customer;
import com.smartretail.backend.models.Product;
import com.smartretail.backend.repository.BillRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class BillServiceImpl implements BillService {
    private static final Logger logger = LoggerFactory.getLogger(BillServiceImpl.class);
    private final BillRepository billRepository;
    private final CustomerService customerService;
    private final ProductService productService;
    private final NotificationService notificationService;
    private final MessageSource messageSource;

    public BillServiceImpl(BillRepository billRepository, CustomerService customerService,
                           ProductService productService, NotificationService notificationService,
                           MessageSource messageSource) {
        this.billRepository = billRepository;
        this.customerService = customerService;
        this.productService = productService;
        this.notificationService = notificationService;
        this.messageSource = messageSource;
    }

    @Override
    @Transactional
    public Bill createBill(Bill bill, Locale locale) {
        logger.debug("[SERVICE] Creating bill: {}", bill.getBillId());

        // Validate required fields
        if (bill.getAddedBy() == null || bill.getAddedBy().isEmpty()) {
            logger.error("[SERVICE] AddedBy field is required for bill: {}", bill.getBillId());
            throw new RuntimeException(
                    messageSource.getMessage("bill.addedBy.missing.service", new Object[]{bill.getBillId()}, locale));
        }

        if (billRepository.existsByBillId(bill.getBillId())) {
            logger.error("[SERVICE] Create failed: Bill ID already exists: {}", bill.getBillId());
            throw new RuntimeException(
                    messageSource.getMessage("bill.exists", new Object[]{bill.getBillId()}, locale));
        }

        // Validate items, calculate total, and update product quantities
        double calculatedTotal = 0.0;
        for (Bill.BillItem item : bill.getItems()) {
            Product product = productService.getProductById(item.getProductId(), locale);
            if (item.getQty() <= 0) {
                logger.error("[SERVICE] Invalid quantity for product {} in bill {}", item.getProductId(), bill.getBillId());
                throw new RuntimeException(
                        messageSource.getMessage("bill.items.invalid.quantity", new Object[]{bill.getBillId()}, locale));
            }
            if (product.getPrice() <= 0) {
                logger.error("[SERVICE] Invalid price for product {} in bill {}", item.getProductId(), bill.getBillId());
                throw new RuntimeException(
                        messageSource.getMessage("bill.items.invalid.price", new Object[]{bill.getBillId()}, locale));
            }
            calculatedTotal += product.getPrice() * item.getQty();
            productService.updateProductQuantity(item.getProductId(), item.getQty(), locale);
        }

        // Set the calculated total amount (remove validation since we're calculating it)
        bill.setTotalAmount(calculatedTotal);

        // Rest of your existing code remains the same...
        // Link bill to customer
        if (bill.getCustomer() != null && bill.getCustomer().getMobile() != null) {
            Customer customer = customerService.findOrCreateCustomer(bill.getCustomer(), locale);
            customerService.addBillId(customer.getMobile(), bill.getBillId());

            // Update customer info in bill with complete customer data
            if (bill.getCustomer() == null) {
                bill.setCustomer(new Bill.CustomerInfo());
            }
            bill.getCustomer().setName(customer.getName());
            bill.getCustomer().setEmail(customer.getEmail());
            bill.getCustomer().setMobile(customer.getMobile());
        }

        // Set timestamps and token
        if (bill.getCreatedAt() == null) {
            bill.setCreatedAt(new Date());
        }
        bill.setPdfAccessToken(UUID.randomUUID().toString());

        // Save bill
        Bill savedBill = billRepository.save(bill);
        logger.info("[SERVICE] Bill created successfully: {}", savedBill.getBillId());

        // Send notification
        if (bill.getCustomer() != null && bill.getCustomer().getEmail() != null) {
            try {
                notificationService.sendBillNotification(
                        bill.getCustomer().getEmail(),
                        bill.getBillId(),
                        bill.getTotalAmount(),
                        bill.getPdfAccessToken(),
                        locale
                );
                logger.info("[SERVICE] Notification sent for bill: {}", bill.getBillId());
            } catch (Exception e) {
                logger.error("[SERVICE] Failed to send notification for bill {}: {}", bill.getBillId(), e.getMessage());
                // Continue even if notification fails
            }
        }

        return savedBill;
    }

    @Override
    public Bill getBillById(String billId, Locale locale) {
        logger.debug("[SERVICE] Fetching bill with ID: {}", billId);
        return billRepository.findByBillId(billId)
                .orElseThrow(() -> {
                    logger.error("[SERVICE] Bill not found for ID: {}", billId);
                    return new RuntimeException(
                            messageSource.getMessage("bill.not.found", new Object[]{billId}, locale));
                });
    }

    @Override
    public List<Bill> getAllBills() {
        logger.debug("[SERVICE] Fetching all bills.");
        return billRepository.findAll();
    }

    @Override
    public boolean validatePdfAccessToken(String billId, String token) {
        logger.debug("[SERVICE] Validating PDF access token for bill: {}", billId);
        return billRepository.findByBillId(billId)
                .map(bill -> token != null && token.equals(bill.getPdfAccessToken()))
                .orElse(false);
    }

    @Override
    public boolean existsByBillId(String billId) {
        logger.debug("[SERVICE] Checking if bill exists: {}", billId);
        return billRepository.existsByBillId(billId);
    }
}