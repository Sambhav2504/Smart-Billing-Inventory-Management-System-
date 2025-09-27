package com.smartretail.backend.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.bean.CsvToBeanBuilder;
import com.smartretail.backend.models.Bill;
import com.smartretail.backend.service.BillService;
import com.smartretail.backend.service.BillPdfService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("/api/billing")
public class BillController {

    private static final Logger logger = LoggerFactory.getLogger(BillController.class);
    private final BillService billService;
    private final BillPdfService billPdfService;
    private final MessageSource messageSource;
    private final ObjectMapper objectMapper;

    public BillController(BillService billService, BillPdfService billPdfService, MessageSource messageSource) {
        this.billService = billService;
        this.billPdfService = billPdfService;
        this.messageSource = messageSource;
        this.objectMapper = new ObjectMapper();
    }

    @PostMapping
    public ResponseEntity<Bill> createBill(@RequestBody Bill bill, Locale locale) {
        logger.debug("Creating bill: {}", bill.getBillId());
        Bill createdBill = billService.createBill(bill, locale);
        return new ResponseEntity<>(createdBill, HttpStatus.CREATED);
    }

    @GetMapping("/{billId}")
    public ResponseEntity<Bill> getBillById(@PathVariable String billId, Locale locale) {
        logger.debug("Fetching bill: {}", billId);
        Bill bill = billService.getBillById(billId, locale);
        return ResponseEntity.ok(bill);
    }

    @GetMapping
    public ResponseEntity<List<Bill>> getAllBills() {
        logger.debug("Fetching all bills");
        List<Bill> bills = billService.getAllBills();
        return ResponseEntity.ok(bills);
    }

    @GetMapping("/{billId}/pdf")
    public ResponseEntity<byte[]> getBillPdf(@PathVariable String billId, @RequestParam(required = false) String token, Locale locale) {
        logger.debug("Fetching PDF for bill: {}", billId);
        if (token != null && billService.validatePdfAccessToken(billId, token)) {
            Bill bill = billService.getBillById(billId, locale);
            byte[] pdfBytes = billPdfService.generateBillPdf(bill, locale);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "bill_" + billId + ".pdf");
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }

    @PostMapping("/bulk")
    public ResponseEntity<Map<String, Object>> bulkUploadBills(
            @RequestPart("file") MultipartFile file,
            Locale locale) {
        logger.info("Starting bulk bill upload");
        if (file == null || file.isEmpty()) {
            logger.error("File is missing");
            throw new IllegalArgumentException(
                    messageSource.getMessage("file.missing", null, locale));
        }

        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".csv")) {
            logger.error("Invalid file format: {}", filename);
            throw new IllegalArgumentException(
                    messageSource.getMessage("file.invalid.format", null, locale));
        }

        Map<String, Object> result = new HashMap<>();
        List<Bill> successfulBills = new ArrayList<>();
        List<String> skippedBills = new ArrayList<>();
        List<String> errorMessages = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {

            List<BillCsvBean> csvBeans;
            try {
                csvBeans = new CsvToBeanBuilder<BillCsvBean>(reader)
                        .withType(BillCsvBean.class)
                        .withIgnoreLeadingWhiteSpace(true)
                        .withIgnoreEmptyLine(true)
                        .build()
                        .parse();
            } catch (Exception e) {
                logger.error("Failed to parse CSV file: {}", e.getMessage());
                throw new IllegalArgumentException(
                        messageSource.getMessage("file.parse.error", null, locale), e);
            }

            if (csvBeans.isEmpty()) {
                errorMessages.add(messageSource.getMessage("file.empty", null, locale));
            }

            for (int i = 0; i < csvBeans.size(); i++) {
                BillCsvBean csvBean = csvBeans.get(i);
                int rowNumber = i + 2; // Account for header row

                try {
                    // Validate required fields
                    if (csvBean.getBillId() == null || csvBean.getBillId().trim().isEmpty()) {
                        errorMessages.add(messageSource.getMessage("bill.id.missing", new Object[]{rowNumber}, locale));
                        continue;
                    }
                    if (csvBean.getCustomerMobile() == null || csvBean.getCustomerMobile().trim().isEmpty()) {
                        errorMessages.add(messageSource.getMessage("bill.customer.missing", new Object[]{rowNumber}, locale));
                        continue;
                    }
                    if (csvBean.getItems() == null || csvBean.getItems().trim().isEmpty()) {
                        errorMessages.add(messageSource.getMessage("bill.items.missing", new Object[]{rowNumber}, locale));
                        continue;
                    }
                    if (csvBean.getAddedBy() == null || csvBean.getAddedBy().trim().isEmpty()) {
                        errorMessages.add(messageSource.getMessage("bill.addedBy.missing", new Object[]{rowNumber}, locale));
                        continue;
                    }

                    // Check for duplicate bill ID
                    if (billService.existsByBillId(csvBean.getBillId().trim())) {
                        skippedBills.add(messageSource.getMessage("bill.skipped", new Object[]{rowNumber, csvBean.getBillId()}, locale));
                        continue;
                    }

                    // Parse items JSON - remove price field and use only productId and quantity
                    List<Bill.BillItem> items;
                    try {
                        String itemsJson = csvBean.getItems().trim();

                        // Parse as generic list to remove price field
                        List<Map<String, Object>> rawItems = objectMapper.readValue(itemsJson, new TypeReference<List<Map<String, Object>>>() {});
                        items = new ArrayList<>();

                        for (Map<String, Object> rawItem : rawItems) {
                            Bill.BillItem item = new Bill.BillItem();

                            // Set productId and quantity only
                            if (rawItem.containsKey("productId")) {
                                item.setProductId((String) rawItem.get("productId"));
                            } else {
                                throw new IllegalArgumentException("productId is required in items");
                            }

                            if (rawItem.containsKey("qty")) {
                                // Handle different number types (Integer, Long, etc.)
                                Number qtyNumber = (Number) rawItem.get("qty");
                                item.setQty(qtyNumber.intValue());
                            } else {
                                throw new IllegalArgumentException("qty is required in items");
                            }

                            // Don't set price - it will be taken from database
                            items.add(item);
                        }

                        // Validate items
                        if (items == null || items.isEmpty()) {
                            errorMessages.add(messageSource.getMessage("bill.items.empty", new Object[]{rowNumber, csvBean.getBillId()}, locale));
                            continue;
                        }

                        for (Bill.BillItem item : items) {
                            if (item.getProductId() == null || item.getProductId().trim().isEmpty()) {
                                errorMessages.add(messageSource.getMessage("bill.item.product.missing", new Object[]{rowNumber, csvBean.getBillId()}, locale));
                                continue;
                            }
                            if (item.getQty() <= 0) {
                                errorMessages.add(messageSource.getMessage("bill.item.quantity.invalid", new Object[]{rowNumber, csvBean.getBillId()}, locale));
                                continue;
                            }
                        }

                    } catch (Exception e) {
                        logger.error("Error parsing items JSON at row {}: {}", rowNumber, e.getMessage());
                        errorMessages.add(messageSource.getMessage("bill.items.invalid", new Object[]{rowNumber, csvBean.getBillId()}, locale));
                        continue;
                    }

                    // Create bill object
                    Bill bill = new Bill();
                    bill.setBillId(csvBean.getBillId().trim());
                    bill.setAddedBy(csvBean.getAddedBy().trim());

                    // Create and set customer info
                    Bill.CustomerInfo customerInfo = new Bill.CustomerInfo();
                    customerInfo.setMobile(csvBean.getCustomerMobile().trim());
                    bill.setCustomer(customerInfo);

                    bill.setItems(items);
                    // Don't set totalAmount - it will be calculated by service based on database prices

                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    dateFormat.setLenient(false);
                    try {
                        if (csvBean.getCreatedAt() != null && !csvBean.getCreatedAt().trim().isEmpty()) {
                            bill.setCreatedAt(dateFormat.parse(csvBean.getCreatedAt().trim()));
                        } else {
                            bill.setCreatedAt(new Date());
                        }
                    } catch (Exception e) {
                        errorMessages.add(messageSource.getMessage("bill.date.invalid", new Object[]{rowNumber, csvBean.getBillId()}, locale));
                        continue;
                    }

                    // Create bill
                    Bill savedBill = billService.createBill(bill, locale);
                    successfulBills.add(savedBill);
                    logger.info("Row {}: Successfully added bill {}", rowNumber, bill.getBillId());

                } catch (Exception e) {
                    // Use a default message if the message code is not found
                    String errorMsg;
                    try {
                        errorMsg = messageSource.getMessage("bill.add.failed", new Object[]{csvBean.getBillId(), rowNumber}, locale) + ": " + e.getMessage();
                    } catch (Exception msgEx) {
                        errorMsg = "Failed to add bill " + csvBean.getBillId() + " at row " + rowNumber + ": " + e.getMessage();
                    }
                    errorMessages.add(errorMsg);
                    logger.error("Error processing row {} for bill {}: {}", rowNumber, csvBean.getBillId(), e.getMessage());
                }
            }

        } catch (IllegalArgumentException e) {
            // Re-throw our custom exceptions
            throw e;
        } catch (Exception e) {
            logger.error("Failed to process CSV file: {}", e.getMessage());
            throw new IllegalArgumentException(
                    messageSource.getMessage("file.processing.error", null, locale), e);
        }

        // Build response
        result.put("successful", successfulBills.size());
        result.put("skipped", skippedBills.size());
        result.put("errors", errorMessages.size());
        result.put("successfulBills", successfulBills);
        result.put("skippedDetails", skippedBills);
        result.put("errorDetails", errorMessages);

        logger.info("Bulk upload completed: {} successful, {} skipped, {} errors",
                successfulBills.size(), skippedBills.size(), errorMessages.size());

        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    public static class BillCsvBean {
        private String billId;
        private String customerMobile;
        private String items;
        private String createdAt;
        private String addedBy;

        // Remove totalAmount from CSV bean since it will be calculated by service
        public String getBillId() { return billId; }
        public void setBillId(String billId) { this.billId = billId; }
        public String getCustomerMobile() { return customerMobile; }
        public void setCustomerMobile(String customerMobile) { this.customerMobile = customerMobile; }
        public String getItems() { return items; }
        public void setItems(String items) { this.items = items; }
        public String getCreatedAt() { return createdAt; }
        public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
        public String getAddedBy() { return addedBy; }
        public void setAddedBy(String addedBy) { this.addedBy = addedBy; }
    }
}