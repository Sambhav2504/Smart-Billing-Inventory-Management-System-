package com.smartretail.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.bean.CsvToBeanBuilder;
import com.smartretail.backend.dto.ProductRequest;
import com.smartretail.backend.models.Product;
import com.smartretail.backend.service.FileService;
import com.smartretail.backend.service.ProductService;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);
    private final ProductService productService;
    private final FileService fileService;
    private final MessageSource messageSource;

    public ProductController(ProductService productService, FileService fileService, MessageSource messageSource) {
        this.productService = productService;
        this.fileService = fileService;
        this.messageSource = messageSource;
    }

    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Map<String, String>> createProduct(
            @RequestParam("product") String productJson,
            @RequestPart(value = "imageFile", required = false) MultipartFile imageFile,
            Locale locale) throws IOException {

        // This handles the multipart case where product is sent as JSON string
        return createProductInternal(productJson, imageFile, locale);
    }

    // Helper method that does the actual work
    private ResponseEntity<Map<String, String>> createProductInternal(String productJson,
                                                                      MultipartFile imageFile, Locale locale) throws IOException {

        ObjectMapper objectMapper = new ObjectMapper();
        ProductRequest request = objectMapper.readValue(productJson, ProductRequest.class);

        Product product = convertToProductEntity(request);
        Product createdProduct = productService.createProduct(product, imageFile, locale);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Product added successfully");
        response.put("productId", createdProduct.getProductId());

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{productId}")
    public ResponseEntity<Product> getProductById(@PathVariable String productId, Locale locale) {
        logger.debug("Fetching product: {}", productId);
        Product product = productService.getProductById(productId, locale);
        return ResponseEntity.ok(product);
    }

    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        logger.debug("Fetching all products");
        List<Product> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    private Product convertToProductEntity(ProductRequest request) {
        Product product = new Product();
        product.setProductId(request.getProductId());
        product.setName(request.getName());
        product.setCategory(request.getCategory());
        product.setPrice(request.getPrice());
        product.setQuantity(request.getQuantity());

        // Handle expiry date parsing if provided
        if (request.getExpiryDate() != null && !request.getExpiryDate().isEmpty()) {
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                product.setExpiryDate(dateFormat.parse(request.getExpiryDate()));
            } catch (Exception e) {
                logger.warn("Failed to parse expiry date: {}", request.getExpiryDate());
                // Continue without expiry date
            }
        }

        // Set defaults for required fields
        product.setMinQuantity(request.getMinQuantity() > 0 ? request.getMinQuantity() : 5);
        product.setReorderLevel(request.getReorderLevel() > 0 ? request.getReorderLevel() : 10);
        product.setSupplierEmail(request.getSupplierEmail() != null ? request.getSupplierEmail() : "default@supplier.com");
        product.setImageUrl(request.getImageUrl()); // This will set imageUrl, imageId will be handled by service
        product.setAddedBy("system"); // Default value, you might want to get this from authentication

        return product;
    }

    @PutMapping("/{productId}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Map<String, String>> updateProduct(
            @PathVariable String productId,
            @RequestBody Map<String, Object> updates,
            Locale locale) {

        logger.debug("Updating product: {}", productId);

        // Convert the updates map to a Product object with only the changed fields
        Product productUpdates = new Product();

        if (updates.containsKey("price")) {
            productUpdates.setPrice(Double.parseDouble(updates.get("price").toString()));
        }
        if (updates.containsKey("quantity")) {
            productUpdates.setQuantity(Integer.parseInt(updates.get("quantity").toString()));
        }
        if (updates.containsKey("name")) {
            productUpdates.setName(updates.get("name").toString());
        }
        if (updates.containsKey("category")) {
            productUpdates.setCategory(updates.get("category").toString());
        }
        if (updates.containsKey("minQuantity")) {
            productUpdates.setMinQuantity(Integer.parseInt(updates.get("minQuantity").toString()));
        }
        if (updates.containsKey("reorderLevel")) {
            productUpdates.setReorderLevel(Integer.parseInt(updates.get("reorderLevel").toString()));
        }

        // Use your existing service method
        Product updatedProduct = productService.updateProduct(productId, productUpdates, locale);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Product updated successfully");
        response.put("productId", updatedProduct.getProductId());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/bulk")
    public ResponseEntity<Map<String, Object>> bulkUploadProducts(
            @RequestPart("file") MultipartFile file,
            Locale locale) {
        logger.info("Starting bulk product upload");

        // File validation
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(messageSource.getMessage("file.missing", null, locale));
        }
        if (!file.getOriginalFilename().toLowerCase().endsWith(".csv")) {
            throw new IllegalArgumentException(messageSource.getMessage("file.invalid.format", null, locale));
        }

        Map<String, Object> result = new HashMap<>();
        List<Product> successfulProducts = new ArrayList<>();
        List<String> skippedProducts = new ArrayList<>();
        List<String> errorMessages = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            List<ProductCsvBean> csvBeans = new CsvToBeanBuilder<ProductCsvBean>(reader)
                    .withType(ProductCsvBean.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build()
                    .parse();

            for (int i = 0; i < csvBeans.size(); i++) {
                ProductCsvBean csvBean = csvBeans.get(i);
                int rowNumber = i + 2;

                try {
                    // Validate required fields
                    if (csvBean.getProductId() == null || csvBean.getProductId().isEmpty()) {
                        errorMessages.add("Row " + rowNumber + ": Product ID is missing");
                        continue;
                    }
                    if (csvBean.getName() == null || csvBean.getName().isEmpty()) {
                        errorMessages.add("Row " + rowNumber + ": Product name is missing");
                        continue;
                    }

                    // Check if product already exists using service
                    if (productService.existsByProductId(csvBean.getProductId())) {
                        skippedProducts.add("Row " + rowNumber + ": Product " + csvBean.getProductId() + " already exists - skipped");
                        continue;
                    }

                    // Create and save product
                    Product newProduct = createProductFromCsvBean(csvBean);
                    Product savedProduct = productService.addProduct(newProduct, locale);
                    successfulProducts.add(savedProduct);
                    logger.info("Row {}: Successfully added product {}", rowNumber, newProduct.getProductId());

                } catch (Exception e) {
                    errorMessages.add("Row " + rowNumber + ": " + e.getMessage());
                }
            }

        } catch (Exception e) {
            logger.error("Failed to parse CSV file: {}", e.getMessage());
            throw new IllegalArgumentException(messageSource.getMessage("file.parse.error", null, locale), e);
        }

        // Build response
        result.put("successful", successfulProducts.size());
        result.put("skipped", skippedProducts.size());
        result.put("errors", errorMessages.size());
        result.put("successfulProducts", successfulProducts);
        result.put("skippedDetails", skippedProducts);
        result.put("errorDetails", errorMessages);

        logger.info("Bulk upload completed: {} successful, {} skipped, {} errors",
                successfulProducts.size(), skippedProducts.size(), errorMessages.size());

        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    private Product createProductFromCsvBean(ProductCsvBean csvBean) {
        Product product = new Product();
        product.setProductId(csvBean.getProductId());
        product.setName(csvBean.getName());
        product.setCategory(csvBean.getCategory());
        product.setPrice(csvBean.getPrice());
        product.setQuantity(csvBean.getQuantity());
        product.setMinQuantity(csvBean.getMinQuantity());
        product.setReorderLevel(csvBean.getReorderLevel());
        product.setAddedBy(csvBean.getAddedBy());

        // Date parsing logic
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setLenient(false);

        try {
            if (csvBean.getExpiryDate() != null && !csvBean.getExpiryDate().isEmpty()) {
                product.setExpiryDate(dateFormat.parse(csvBean.getExpiryDate()));
            }
            if (csvBean.getLastUpdated() != null && !csvBean.getLastUpdated().isEmpty()) {
                product.setLastUpdated(dateFormat.parse(csvBean.getLastUpdated()));
            } else {
                product.setLastUpdated(new Date());
            }
        } catch (Exception e) {
            product.setLastUpdated(new Date());
        }

        product.setCreatedAt(new Date());

        return product;
    }

    @Setter
    @Getter
    public static class ProductCsvBean {
        private String productId;
        private String name;
        private String category;
        private double price;
        private int quantity;
        private int minQuantity;
        private int reorderLevel;
        private String expiryDate;
        private String addedBy;
        private String lastUpdated;
    }
}