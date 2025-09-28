package com.smartretail.backend.service;

import com.smartretail.backend.models.Bill;
import com.smartretail.backend.models.Customer;
import com.smartretail.backend.models.Product;
import com.smartretail.backend.repository.BillRepository;
import com.smartretail.backend.repository.CustomerRepository;
import com.smartretail.backend.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
public class BillServiceImpl implements BillService {
    private static final Logger logger = LoggerFactory.getLogger(BillServiceImpl.class);
    private final BillRepository billRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;
    private final NotificationService notificationService;
    private final PdfService pdfService;
    private final MessageSource messageSource;
    private final AuditLogService auditLogService;

    public BillServiceImpl(BillRepository billRepository,
                           CustomerRepository customerRepository,
                           ProductRepository productRepository,
                           ProductService productService,
                           NotificationService notificationService,
                           PdfService pdfService,
                           MessageSource messageSource,
                           AuditLogService auditLogService) {
        this.billRepository = billRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.productService = productService;
        this.notificationService = notificationService;
        this.pdfService = pdfService;
        this.messageSource = messageSource;
        this.auditLogService = auditLogService;
    }

    @Override
    @Transactional
    public Bill createBill(Bill bill, Locale locale) {
        return createBill(bill, locale, false);
    }

    @Override
    @Transactional
    public Bill createBill(Bill bill, Locale locale, boolean isSyncMode) {
        logger.debug("[BILL SERVICE] Creating bill with ID: {}, sync mode: {}", bill.getBillId(), isSyncMode);

        // Check for duplicate bill ID in sync mode
        if (isSyncMode && billRepository.existsByBillId(bill.getBillId())) {
            logger.info("[BILL SERVICE] Bill {} already exists, skipping in sync mode", bill.getBillId());
            return billRepository.findByBillId(bill.getBillId()).orElse(bill);
        }

        // Validate required fields for non-sync mode
        if (!isSyncMode) {
            if (bill.getAddedBy() == null || bill.getAddedBy().isEmpty()) {
                logger.error("[BILL SERVICE] AddedBy field is required for bill: {}", bill.getBillId());
                throw new RuntimeException(
                        messageSource.getMessage("bill.addedBy.missing.service", new Object[]{bill.getBillId()}, locale));
            }
        }

        // Validate duplicate bill ID for non-sync mode
        if (!isSyncMode && billRepository.existsByBillId(bill.getBillId())) {
            logger.error("[BILL SERVICE] Create failed: Bill ID already exists: {}", bill.getBillId());
            throw new RuntimeException(
                    messageSource.getMessage("bill.exists", new Object[]{bill.getBillId()}, locale));
        }

        // Validate customer
        if (bill.getCustomer() == null || bill.getCustomer().getMobile() == null) {
            throw new IllegalArgumentException(
                    messageSource.getMessage("bill.customer.required", null, locale));
        }

        // Process items and calculate total
        double calculatedTotal = 0.0;
        for (Bill.BillItem item : bill.getItems()) {
            Product product = productService.getProductById(item.getProductId(), locale);

            // Set product name and price in the bill item
            item.setProductName(product.getName());
            item.setPrice(product.getPrice());

            if (item.getQty() <= 0) {
                logger.error("[BILL SERVICE] Invalid quantity for product {} in bill {}", item.getProductId(), bill.getBillId());
                throw new RuntimeException(
                        messageSource.getMessage("bill.items.invalid.quantity", new Object[]{bill.getBillId()}, locale));
            }
            if (product.getPrice() <= 0) {
                logger.error("[BILL SERVICE] Invalid price for product {} in bill {}", item.getProductId(), bill.getBillId());
                throw new RuntimeException(
                        messageSource.getMessage("bill.items.invalid.price", new Object[]{bill.getBillId()}, locale));
            }
            calculatedTotal += product.getPrice() * item.getQty();

            // Update product quantity
            if (!isSyncMode) {
                productService.updateProductQuantity(item.getProductId(), item.getQty(), locale);
            } else {
                // For sync mode, use direct repository update
                int newQuantity = product.getQuantity() - item.getQty();
                if (newQuantity < 0) {
                    throw new IllegalArgumentException(
                            messageSource.getMessage("product.out.of.stock", new Object[]{item.getProductName()}, locale));
                }
                product.setQuantity(newQuantity);
                productRepository.save(product);
            }
        }

        // Set the calculated total amount
        bill.setTotalAmount(calculatedTotal);

        // Process customer
        logger.debug("[BILL SERVICE] Processing customer with mobile: {}", bill.getCustomer().getMobile());
        Customer customer = customerRepository.findByMobile(bill.getCustomer().getMobile())
                .orElseGet(() -> {
                    Customer newCustomer = new Customer();
                    newCustomer.setMobile(bill.getCustomer().getMobile());
                    newCustomer.setName(bill.getCustomer().getName());
                    newCustomer.setEmail(bill.getCustomer().getEmail());
                    logger.info("[BILL SERVICE] Creating new customer with mobile: {}", bill.getCustomer().getMobile());
                    return customerRepository.save(newCustomer);
                });

        logger.debug("[BILL SERVICE] Processing customer - Name: {}, Email: {}, Mobile: {}",
                customer.getName(), customer.getEmail(), customer.getMobile());

        // Update customer purchase history
        customer.getPurchaseHistory().add(bill.getBillId());
        customerRepository.save(customer);

        // Create updated customer info for bill
        Bill.CustomerInfo updatedCustomerInfo = new Bill.CustomerInfo();
        updatedCustomerInfo.setName(customer.getName());
        updatedCustomerInfo.setEmail(customer.getEmail());
        updatedCustomerInfo.setMobile(customer.getMobile());
        bill.setCustomer(updatedCustomerInfo);

        logger.debug("[BILL SERVICE] Updated bill customer - Name: {}, Email: {}, Mobile: {}",
                bill.getCustomer().getName(), bill.getCustomer().getEmail(), bill.getCustomer().getMobile());

        // Set timestamps and token
        if (bill.getCreatedAt() == null) {
            bill.setCreatedAt(new Date());
        }
        bill.setPdfAccessToken(UUID.randomUUID().toString());

        // Save bill
        Bill savedBill = billRepository.save(bill);
        logger.info("[BILL SERVICE] Bill created successfully: {}", savedBill.getBillId());

        // Log audit for bill creation (only in non-sync mode)
        if (!isSyncMode) {
            String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
            Map<String, Object> auditDetails = new HashMap<>();
            auditDetails.put("billId", savedBill.getBillId());
            auditDetails.put("customerName", bill.getCustomer() != null ? bill.getCustomer().getName() : "N/A");
            auditDetails.put("customerMobile", bill.getCustomer() != null ? bill.getCustomer().getMobile() : "N/A");
            auditDetails.put("totalAmount", savedBill.getTotalAmount());
            auditDetails.put("itemCount", savedBill.getItems().size());
            auditDetails.put("addedBy", savedBill.getAddedBy());
            auditDetails.put("syncMode", isSyncMode);

            auditLogService.logAction("BILL_CREATED", savedBill.getBillId(), userEmail, auditDetails);
        }

        // Send notification (ALWAYS send email, regardless of sync mode)
        if (bill.getCustomer() != null && bill.getCustomer().getEmail() != null) {
            sendBillNotification(savedBill, locale, isSyncMode);
        } else {
            logger.warn("[BILL SERVICE] No email address for customer: {}", bill.getCustomer().getMobile());
        }

        return savedBill;
    }

    private void sendBillNotification(Bill bill, Locale locale, boolean isSyncMode) {
        logger.debug("[BILL SERVICE] Attempting to send email to: {}, sync mode: {}",
                bill.getCustomer().getEmail(), isSyncMode);
        try {
            // Generate PDF content
            byte[] pdfContent = pdfService.generateBillPdf(bill, locale);
            logger.debug("[BILL SERVICE] PDF generated successfully, size: {} bytes", pdfContent.length);

            notificationService.sendBillNotification(
                    bill.getCustomer().getEmail(),
                    bill.getBillId(),
                    bill.getTotalAmount(),
                    pdfContent,
                    locale
            );
            logger.info("[BILL SERVICE] Notification sent for bill: {} to: {}",
                    bill.getBillId(), bill.getCustomer().getEmail());

            // Log audit for notification success
            if (!isSyncMode) {
                String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
                Map<String, Object> notificationDetails = new HashMap<>();
                notificationDetails.put("billId", bill.getBillId());
                notificationDetails.put("customerEmail", bill.getCustomer().getEmail());
                notificationDetails.put("notificationStatus", "SENT");
                notificationDetails.put("syncMode", isSyncMode);
                auditLogService.logAction("BILL_NOTIFICATION_SENT", bill.getBillId(), userEmail, notificationDetails);
            }

        } catch (Exception e) {
            logger.error("[BILL SERVICE] Failed to send notification for bill {}: {}", bill.getBillId(), e.getMessage());

            // Log audit for notification failure
            if (!isSyncMode) {
                String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
                Map<String, Object> notificationDetails = new HashMap<>();
                notificationDetails.put("billId", bill.getBillId());
                notificationDetails.put("customerEmail", bill.getCustomer().getEmail());
                notificationDetails.put("notificationStatus", "FAILED");
                notificationDetails.put("error", e.getMessage());
                notificationDetails.put("syncMode", isSyncMode);
                auditLogService.logAction("BILL_NOTIFICATION_FAILED", bill.getBillId(), userEmail, notificationDetails);
            }
        }
    }

    @Override
    public Bill getBillById(String billId, Locale locale) {
        logger.debug("[BILL SERVICE] Fetching bill with ID: {}", billId);
        Bill bill = billRepository.findByBillId(billId)
                .orElseThrow(() -> {
                    logger.error("[BILL SERVICE] Bill not found for ID: {}", billId);
                    return new RuntimeException(
                            messageSource.getMessage("bill.not.found", new Object[]{billId}, locale));
                });

        // Log audit for bill access
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        Map<String, Object> auditDetails = new HashMap<>();
        auditDetails.put("billId", billId);
        auditDetails.put("customerName", bill.getCustomer() != null ? bill.getCustomer().getName() : "N/A");
        auditDetails.put("totalAmount", bill.getTotalAmount());
        auditLogService.logAction("BILL_ACCESSED", billId, userEmail, auditDetails);

        return bill;
    }

    @Override
    public List<Bill> getAllBills() {
        logger.debug("[BILL SERVICE] Fetching all bills");
        return billRepository.findAll();
    }

    @Override
    public List<Bill> getBillsByDateRange(Date startDate, Date endDate, Locale locale) {
        logger.debug("[BILL SERVICE] Fetching bills from {} to {}", startDate, endDate);
        if (startDate.after(endDate)) {
            throw new IllegalArgumentException(
                    messageSource.getMessage("report.date.range.invalid", new Object[]{startDate, endDate}, locale));
        }
        return billRepository.findByCreatedAtBetween(startDate, endDate);
    }

    @Override
    public boolean validatePdfAccessToken(String billId, String token) {
        logger.debug("[BILL SERVICE] Validating PDF access token for bill: {}", billId);
        boolean isValid = billRepository.findByBillId(billId)
                .map(bill -> token != null && token.equals(bill.getPdfAccessToken()))
                .orElse(false);

        // Log audit for PDF token validation
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        Map<String, Object> auditDetails = new HashMap<>();
        auditDetails.put("billId", billId);
        auditDetails.put("tokenValid", isValid);
        auditDetails.put("validationType", "PDF_ACCESS");
        auditLogService.logAction("BILL_PDF_TOKEN_VALIDATED", billId, userEmail, auditDetails);

        return isValid;
    }

    @Override
    public boolean existsByBillId(String billId) {
        logger.debug("[BILL SERVICE] Checking if bill exists: {}", billId);
        return billRepository.existsByBillId(billId);
    }
}