package com.smartretail.backend.service;

import com.smartretail.backend.models.Bill;
import com.smartretail.backend.models.Customer;
import com.smartretail.backend.models.Product;
import com.smartretail.backend.repository.BillRepository;
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
    private final CustomerService customerService;
    private final ProductService productService;
    private final NotificationService notificationService;
    private final PdfService pdfService;
    private final MessageSource messageSource;
    private final AuditLogService auditLogService;

    public BillServiceImpl(BillRepository billRepository, CustomerService customerService,
                           ProductService productService, NotificationService notificationService,
                           PdfService pdfService, MessageSource messageSource, AuditLogService auditLogService) {
        this.billRepository = billRepository;
        this.customerService = customerService;
        this.productService = productService;
        this.notificationService = notificationService;
        this.pdfService = pdfService;
        this.messageSource = messageSource;
        this.auditLogService = auditLogService;
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
        // In BillServiceImpl.createBill() method, update the items processing:
        for (Bill.BillItem item : bill.getItems()) {
            Product product = productService.getProductById(item.getProductId(), locale);

            // Set product name and price in the bill item
            item.setProductName(product.getName());
            item.setPrice(product.getPrice()); // 🔥 IMPORTANT: Set the price in the bill item

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

        // Set the calculated total amount
        bill.setTotalAmount(calculatedTotal);

        // 🔥 FIXED: Link bill to customer - CORRECTED LOGIC
        if (bill.getCustomer() != null && bill.getCustomer().getMobile() != null) {
            logger.debug("[SERVICE] Processing customer with mobile: {}", bill.getCustomer().getMobile());
            logger.debug("[SERVICE] Original customer email from request: {}", bill.getCustomer().getEmail());

            Customer customer = customerService.findOrCreateCustomer(bill.getCustomer(), locale);
            customerService.addBillId(customer.getMobile(), bill.getBillId());

            // 🔥 CRITICAL FIX: Create a NEW CustomerInfo object with data from database
            Bill.CustomerInfo updatedCustomerInfo = new Bill.CustomerInfo();
            updatedCustomerInfo.setName(customer.getName());
            updatedCustomerInfo.setEmail(customer.getEmail()); // This gets the email from database
            updatedCustomerInfo.setMobile(customer.getMobile());

            // Replace the entire customer object in the bill
            bill.setCustomer(updatedCustomerInfo);

            logger.debug("[SERVICE] Updated bill customer - Name: {}, Email: {}, Mobile: {}",
                    bill.getCustomer().getName(), bill.getCustomer().getEmail(), bill.getCustomer().getMobile());
        }

        // Set timestamps and token
        if (bill.getCreatedAt() == null) {
            bill.setCreatedAt(new Date());
        }
        bill.setPdfAccessToken(UUID.randomUUID().toString());

        // Save bill
        Bill savedBill = billRepository.save(bill);
        logger.info("[SERVICE] Bill created successfully: {}", savedBill.getBillId());

        // Log audit for bill creation
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        Map<String, Object> auditDetails = new HashMap<>();
        auditDetails.put("billId", savedBill.getBillId());
        auditDetails.put("customerName", bill.getCustomer() != null ? bill.getCustomer().getName() : "N/A");
        auditDetails.put("customerMobile", bill.getCustomer() != null ? bill.getCustomer().getMobile() : "N/A");
        auditDetails.put("totalAmount", savedBill.getTotalAmount());
        auditDetails.put("itemCount", savedBill.getItems().size());
        auditDetails.put("addedBy", savedBill.getAddedBy());

        auditLogService.logAction("BILL_CREATED", savedBill.getBillId(), userEmail, auditDetails);

        // Send notification - ADD DEBUG LOGS
        if (bill.getCustomer() != null && bill.getCustomer().getEmail() != null) {
            logger.debug("[SERVICE] Attempting to send email to: {}", bill.getCustomer().getEmail());
            try {
                // Generate PDF content
                byte[] pdfContent = pdfService.generateBillPdf(savedBill, locale);
                logger.debug("[SERVICE] PDF generated successfully, size: {} bytes", pdfContent.length);

                notificationService.sendBillNotification(
                        bill.getCustomer().getEmail(),
                        bill.getBillId(),
                        bill.getTotalAmount(),
                        pdfContent,
                        locale
                );
                logger.info("[SERVICE] Notification sent for bill: {}", bill.getBillId());

                // Log audit for notification success
                Map<String, Object> notificationDetails = new HashMap<>();
                notificationDetails.put("billId", savedBill.getBillId());
                notificationDetails.put("customerEmail", bill.getCustomer().getEmail());
                notificationDetails.put("notificationStatus", "SENT");
                auditLogService.logAction("BILL_NOTIFICATION_SENT", savedBill.getBillId(), userEmail, notificationDetails);

            } catch (Exception e) {
                logger.error("[SERVICE] Failed to send notification for bill {}: {}", bill.getBillId(), e.getMessage());
                e.printStackTrace(); // Add stack trace for debugging

                // Log audit for notification failure
                Map<String, Object> notificationDetails = new HashMap<>();
                notificationDetails.put("billId", savedBill.getBillId());
                notificationDetails.put("customerEmail", bill.getCustomer().getEmail());
                notificationDetails.put("notificationStatus", "FAILED");
                notificationDetails.put("error", e.getMessage());
                auditLogService.logAction("BILL_NOTIFICATION_FAILED", savedBill.getBillId(), userEmail, notificationDetails);
            }
        } else {
            logger.warn("[SERVICE] No customer email available for bill: {}", bill.getBillId());
            if (bill.getCustomer() == null) {
                logger.warn("[SERVICE] Customer object is null");
            } else {
                logger.warn("[SERVICE] Customer email is null");
            }
        }

        return savedBill;
    }

    @Override
    public Bill getBillById(String billId, Locale locale) {
        logger.debug("[SERVICE] Fetching bill with ID: {}", billId);
        Bill bill = billRepository.findByBillId(billId)
                .orElseThrow(() -> {
                    logger.error("[SERVICE] Bill not found for ID: {}", billId);
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
        logger.debug("[SERVICE] Fetching all bills.");

        // Log audit for bulk bill access
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        Map<String, Object> auditDetails = new HashMap<>();
        auditDetails.put("action", "FETCH_ALL_BILLS");
        auditLogService.logAction("BILLS_BULK_ACCESSED", "ALL", userEmail, auditDetails);

        return billRepository.findAll();
    }

    @Override
    public boolean validatePdfAccessToken(String billId, String token) {
        logger.debug("[SERVICE] Validating PDF access token for bill: {}", billId);
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
        logger.debug("[SERVICE] Checking if bill exists: {}", billId);

        // Log audit for bill existence check
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        Map<String, Object> auditDetails = new HashMap<>();
        auditDetails.put("billId", billId);
        auditLogService.logAction("BILL_EXISTENCE_CHECKED", billId, userEmail, auditDetails);

        return billRepository.existsByBillId(billId);
    }
}