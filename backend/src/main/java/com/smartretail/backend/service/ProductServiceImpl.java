package com.smartretail.backend.service;

import com.smartretail.backend.exception.DuplicateResourceException;
import com.smartretail.backend.exception.ResourceNotFoundException;
import com.smartretail.backend.exception.ProductNotFoundException;
import com.smartretail.backend.models.Product;
import com.smartretail.backend.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {
    private static final Logger logger = LoggerFactory.getLogger(ProductServiceImpl.class);
    private final ProductRepository productRepository;
    private final NotificationService notificationService;
    private final FileService fileService;
    private final MessageSource messageSource;
    private final AuditLogService auditLogService;

    public ProductServiceImpl(ProductRepository productRepository,
                              NotificationService notificationService,
                              FileService fileService,
                              MessageSource messageSource,
                              AuditLogService auditLogService) {
        this.productRepository = productRepository;
        this.notificationService = notificationService;
        this.fileService = fileService;
        this.messageSource = messageSource;
        this.auditLogService = auditLogService;
    }

    @Override
    public boolean existsByProductId(String productId) {
        logger.debug("[SERVICE] Checking if product exists: {}", productId);

        // Log audit for existence check
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        Map<String, Object> auditDetails = new HashMap<>();
        auditDetails.put("productId", productId);
        auditLogService.logAction("PRODUCT_EXISTENCE_CHECKED", productId, userEmail, auditDetails);

        return productRepository.existsByProductId(productId);
    }

    @Override
    @Transactional
    public Product addProduct(Product product, Locale locale) {
        logger.debug("[SERVICE] Adding product: {}", product.getName());
        if (productRepository.existsByProductId(product.getProductId())) {
            logger.error("[SERVICE] Add failed: Product ID already exists: {}", product.getProductId());

            // Log audit for duplicate attempt
            String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
            Map<String, Object> auditDetails = new HashMap<>();
            auditDetails.put("productId", product.getProductId());
            auditDetails.put("name", product.getName());
            auditDetails.put("reason", "DUPLICATE_PRODUCT_ID");
            auditLogService.logAction("PRODUCT_CREATION_FAILED", product.getProductId(), userEmail, auditDetails);

            throw new IllegalArgumentException(
                    messageSource.getMessage("product.exists", new Object[]{product.getProductId()}, locale));
        }

        product.setLastUpdated(new Date());
        Product savedProduct = productRepository.save(product);
        logger.info("[SERVICE] Product added successfully: {}", savedProduct.getProductId());

        // Log audit for successful creation
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        Map<String, Object> auditDetails = new HashMap<>();
        auditDetails.put("name", product.getName());
        auditDetails.put("category", product.getCategory());
        auditDetails.put("price", product.getPrice());
        auditDetails.put("quantity", product.getQuantity());
        auditDetails.put("reorderLevel", product.getReorderLevel());
        auditDetails.put("expiryDate", product.getExpiryDate());
        auditLogService.logAction("PRODUCT_CREATED", product.getProductId(), userEmail, auditDetails);

        return savedProduct;
    }

    @Override
    public Product getProductById(String productId, Locale locale) {
        logger.debug("[SERVICE] Fetching product with ID: {}", productId);
        Product product = productRepository.findByProductId(productId)
                .orElseThrow(() -> {
                    logger.error("[SERVICE] Product not found for ID: {}", productId);

                    // Log audit for failed access attempt
                    String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
                    Map<String, Object> auditDetails = new HashMap<>();
                    auditDetails.put("productId", productId);
                    auditDetails.put("reason", "PRODUCT_NOT_FOUND");
                    auditLogService.logAction("PRODUCT_ACCESS_FAILED", productId, userEmail, auditDetails);

                    return new ProductNotFoundException(
                            messageSource.getMessage("product.not.found", new Object[]{productId}, locale));
                });

        // Log audit for successful access
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        Map<String, Object> auditDetails = new HashMap<>();
        auditDetails.put("name", product.getName());
        auditDetails.put("quantity", product.getQuantity());
        auditDetails.put("price", product.getPrice());
        auditLogService.logAction("PRODUCT_ACCESSED", productId, userEmail, auditDetails);

        return product;
    }

    @Override
    public List<Product> getAllProducts() {
        logger.debug("[SERVICE] Fetching all products.");

        // Log audit for bulk access
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        Map<String, Object> auditDetails = new HashMap<>();
        auditDetails.put("action", "FETCH_ALL_PRODUCTS");
        auditLogService.logAction("PRODUCTS_BULK_ACCESSED", "ALL", userEmail, auditDetails);

        return productRepository.findAll();
    }

    @Override
    @Transactional
    public Product createProduct(Product product, MultipartFile imageFile, Locale locale) throws IOException {
        logger.debug("[SERVICE] Creating product: {}", product.getName());
        if (productRepository.existsByProductId(product.getProductId())) {
            logger.error("[SERVICE] Create failed: Product ID already exists: {}", product.getProductId());

            // Log audit for duplicate attempt
            String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
            Map<String, Object> auditDetails = new HashMap<>();
            auditDetails.put("productId", product.getProductId());
            auditDetails.put("name", product.getName());
            auditDetails.put("reason", "DUPLICATE_PRODUCT_ID");
            auditLogService.logAction("PRODUCT_CREATION_FAILED", product.getProductId(), userEmail, auditDetails);

            throw new DuplicateResourceException("product.exists", product.getProductId());
        }

        String imageId = null;
        if (imageFile != null && !imageFile.isEmpty()) {
            imageId = fileService.uploadImage(imageFile, "product_" + product.getProductId());
            product.setImageId(imageId);
        }

        product.setCreatedAt(new Date());
        product.setLastUpdated(new Date());
        Product savedProduct = productRepository.save(product);
        logger.info("[SERVICE] Product created successfully: {}", savedProduct.getProductId());

        // Log audit for successful creation
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        Map<String, Object> auditDetails = new HashMap<>();
        auditDetails.put("name", product.getName());
        auditDetails.put("category", product.getCategory());
        auditDetails.put("price", product.getPrice());
        auditDetails.put("quantity", product.getQuantity());
        auditDetails.put("reorderLevel", product.getReorderLevel());
        auditDetails.put("expiryDate", product.getExpiryDate());
        auditDetails.put("imageId", imageId);
        auditDetails.put("hasImage", imageId != null);
        auditLogService.logAction("PRODUCT_CREATED", product.getProductId(), userEmail, auditDetails);

        return savedProduct;
    }

    @Override
    @Transactional
    public Product updateProduct(String productId, Product product, Locale locale) {
        try {
            return updateProduct(productId, product, null, locale);
        } catch (IOException e) {
            logger.error("[SERVICE] Update failed for product ID {}: {}", productId, e.getMessage());

            // Log audit for update failure
            String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
            Map<String, Object> auditDetails = new HashMap<>();
            auditDetails.put("productId", productId);
            auditDetails.put("error", e.getMessage());
            auditLogService.logAction("PRODUCT_UPDATE_FAILED", productId, userEmail, auditDetails);

            throw new RuntimeException(
                    messageSource.getMessage("product.update.failed", new Object[]{productId}, locale));
        }
    }

    @Override
    @Transactional
    public Product updateProduct(String productId, Product product, MultipartFile imageFile, Locale locale) throws IOException {
        logger.debug("[SERVICE] Updating product ID: {}", productId);
        Product existingProduct = productRepository.findByProductId(productId)
                .orElseThrow(() -> {
                    logger.error("[SERVICE] Product not found for ID: {}", productId);

                    // Log audit for failed update attempt
                    String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
                    Map<String, Object> auditDetails = new HashMap<>();
                    auditDetails.put("productId", productId);
                    auditDetails.put("reason", "PRODUCT_NOT_FOUND");
                    auditLogService.logAction("PRODUCT_UPDATE_FAILED", productId, userEmail, auditDetails);

                    return new ResourceNotFoundException("product.not.found", productId);
                });

        // Track changes for audit log
        Map<String, Object> changes = new HashMap<>();
        Map<String, Object> oldValues = new HashMap<>();
        Map<String, Object> newValues = new HashMap<>();

        if (product.getName() != null && !product.getName().equals(existingProduct.getName())) {
            oldValues.put("name", existingProduct.getName());
            newValues.put("name", product.getName());
            existingProduct.setName(product.getName());
        }
        if (product.getCategory() != null && !product.getCategory().equals(existingProduct.getCategory())) {
            oldValues.put("category", existingProduct.getCategory());
            newValues.put("category", product.getCategory());
            existingProduct.setCategory(product.getCategory());
        }
        if (product.getPrice() > 0 && product.getPrice() != existingProduct.getPrice()) {
            oldValues.put("price", existingProduct.getPrice());
            newValues.put("price", product.getPrice());
            existingProduct.setPrice(product.getPrice());
        }
        if (product.getQuantity() >= 0 && product.getQuantity() != existingProduct.getQuantity()) {
            oldValues.put("quantity", existingProduct.getQuantity());
            newValues.put("quantity", product.getQuantity());
            existingProduct.setQuantity(product.getQuantity());
        }
        if (product.getReorderLevel() >= 0 && product.getReorderLevel() != existingProduct.getReorderLevel()) {
            oldValues.put("reorderLevel", existingProduct.getReorderLevel());
            newValues.put("reorderLevel", product.getReorderLevel());
            existingProduct.setReorderLevel(product.getReorderLevel());
        }
        if (product.getExpiryDate() != null && !product.getExpiryDate().equals(existingProduct.getExpiryDate())) {
            oldValues.put("expiryDate", existingProduct.getExpiryDate());
            newValues.put("expiryDate", product.getExpiryDate());
            existingProduct.setExpiryDate(product.getExpiryDate());
        }

        String oldImageId = existingProduct.getImageId();
        String newImageId = null;
        if (imageFile != null && !imageFile.isEmpty()) {
            newImageId = fileService.uploadImage(imageFile, "product_" + productId);
            existingProduct.setImageId(newImageId);
            oldValues.put("imageId", oldImageId);
            newValues.put("imageId", newImageId);
        } else if (product.getImageId() != null) {
            if (product.getImageId().isEmpty()) {
                existingProduct.setImageId(null);
                oldValues.put("imageId", oldImageId);
                newValues.put("imageId", null);
            } else if (!product.getImageId().equals(oldImageId)) {
                existingProduct.setImageId(product.getImageId());
                oldValues.put("imageId", oldImageId);
                newValues.put("imageId", product.getImageId());
            }
        }

        existingProduct.setLastUpdated(new Date());
        Product updatedProduct = productRepository.save(existingProduct);
        logger.info("[SERVICE] Product updated successfully: {}", updatedProduct.getProductId());

        // Send low stock notification if applicable
        boolean lowStockNotificationSent = false;
        if (updatedProduct.getQuantity() < updatedProduct.getReorderLevel()) {
            notificationService.sendLowStockNotification(
                    "manager@shop.com",
                    updatedProduct.getName(),
                    updatedProduct.getQuantity(),
                    updatedProduct.getReorderLevel()
            );
            lowStockNotificationSent = true;
        }

        // Log audit for successful update
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        Map<String, Object> auditDetails = new HashMap<>();
        auditDetails.put("changes", newValues);
        auditDetails.put("oldValues", oldValues);
        auditDetails.put("lowStockNotificationSent", lowStockNotificationSent);
        auditDetails.put("currentQuantity", updatedProduct.getQuantity());
        auditDetails.put("reorderLevel", updatedProduct.getReorderLevel());
        auditLogService.logAction("PRODUCT_UPDATED", productId, userEmail, auditDetails);

        return updatedProduct;
    }

    @Override
    @Transactional
    public void deleteProduct(String productId, Locale locale) {
        logger.debug("[SERVICE] Deleting product ID: {}", productId);
        Product product = productRepository.findByProductId(productId)
                .orElseThrow(() -> {
                    logger.error("[SERVICE] Delete failed: Product not found for ID: {}", productId);

                    // Log audit for failed deletion attempt
                    String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
                    Map<String, Object> auditDetails = new HashMap<>();
                    auditDetails.put("productId", productId);
                    auditDetails.put("reason", "PRODUCT_NOT_FOUND");
                    auditLogService.logAction("PRODUCT_DELETION_FAILED", productId, userEmail, auditDetails);

                    return new ProductNotFoundException(
                            messageSource.getMessage("product.not.found", new Object[]{productId}, locale));
                });

        // Log product details before deletion for audit
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        Map<String, Object> auditDetails = new HashMap<>();
        auditDetails.put("name", product.getName());
        auditDetails.put("category", product.getCategory());
        auditDetails.put("quantity", product.getQuantity());
        auditDetails.put("price", product.getPrice());

        productRepository.deleteByProductId(productId);
        logger.info("[SERVICE] Product deleted successfully: {}", productId);

        auditLogService.logAction("PRODUCT_DELETED", productId, userEmail, auditDetails);
    }

    @Override
    public List<Product> getLowStockProducts() {
        logger.debug("[SERVICE] Fetching low stock products.");

        // Log audit for low stock query
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        Map<String, Object> auditDetails = new HashMap<>();
        auditDetails.put("action", "FETCH_LOW_STOCK_PRODUCTS");
        auditLogService.logAction("LOW_STOCK_PRODUCTS_ACCESSED", "LOW_STOCK", userEmail, auditDetails);

        return productRepository.findLowStockProducts();
    }

    @Override
    public List<Product> getExpiringProducts(Date threshold) {
        logger.debug("[SERVICE] Fetching expiring products before: {}", threshold);

        // Log audit for expiring products query
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        Map<String, Object> auditDetails = new HashMap<>();
        auditDetails.put("threshold", threshold);
        auditDetails.put("action", "FETCH_EXPIRING_PRODUCTS");
        auditLogService.logAction("EXPIRING_PRODUCTS_ACCESSED", "EXPIRING", userEmail, auditDetails);

        return productRepository.findByExpiryDateBefore(threshold).stream()
                .filter(product -> product.getExpiryDate() != null)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Product restockProduct(String productId, int restockQty, Locale locale) {
        logger.debug("[SERVICE] Restocking product ID: {} with quantity: {}", productId, restockQty);
        if (restockQty <= 0) {
            logger.error("[SERVICE] Restock failed: Invalid quantity: {}", restockQty);

            // Log audit for failed restock
            String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
            Map<String, Object> auditDetails = new HashMap<>();
            auditDetails.put("productId", productId);
            auditDetails.put("restockQty", restockQty);
            auditDetails.put("reason", "INVALID_QUANTITY");
            auditLogService.logAction("PRODUCT_RESTOCK_FAILED", productId, userEmail, auditDetails);

            throw new IllegalArgumentException(
                    messageSource.getMessage("product.restock.invalid.quantity", new Object[]{restockQty}, locale));
        }

        Product product = productRepository.findByProductId(productId)
                .orElseThrow(() -> {
                    logger.error("[SERVICE] Restock failed: Product not found for ID: {}", productId);

                    // Log audit for failed restock
                    String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
                    Map<String, Object> auditDetails = new HashMap<>();
                    auditDetails.put("productId", productId);
                    auditDetails.put("restockQty", restockQty);
                    auditDetails.put("reason", "PRODUCT_NOT_FOUND");
                    auditLogService.logAction("PRODUCT_RESTOCK_FAILED", productId, userEmail, auditDetails);

                    return new ProductNotFoundException(
                            messageSource.getMessage("product.not.found", new Object[]{productId}, locale));
                });

        int oldQuantity = product.getQuantity();
        product.setQuantity(oldQuantity + restockQty);
        product.setLastUpdated(new Date());
        Product updatedProduct = productRepository.save(product);

        boolean lowStockNotificationSent = false;
        if (updatedProduct.getQuantity() < updatedProduct.getReorderLevel()) {
            notificationService.sendLowStockNotification(
                    "manager@shop.com",
                    updatedProduct.getName(),
                    updatedProduct.getQuantity(),
                    updatedProduct.getReorderLevel()
            );
            lowStockNotificationSent = true;
        }

        logger.info("[SERVICE] Product restocked successfully: {}", updatedProduct.getProductId());

        // Log audit for successful restock
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        Map<String, Object> auditDetails = new HashMap<>();
        auditDetails.put("name", product.getName());
        auditDetails.put("oldQuantity", oldQuantity);
        auditDetails.put("restockQty", restockQty);
        auditDetails.put("newQuantity", updatedProduct.getQuantity());
        auditDetails.put("lowStockNotificationSent", lowStockNotificationSent);
        auditLogService.logAction("PRODUCT_RESTOCKED", productId, userEmail, auditDetails);

        return updatedProduct;
    }

    @Override
    @Transactional
    public Product updateProductQuantity(String productId, int quantity, Locale locale) {
        logger.debug("[SERVICE] Updating quantity for product ID: {} by: {}", productId, quantity);
        Product product = productRepository.findByProductId(productId)
                .orElseThrow(() -> {
                    logger.error("[SERVICE] Quantity update failed: Product not found for ID: {}", productId);

                    // Log audit for failed quantity update
                    String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
                    Map<String, Object> auditDetails = new HashMap<>();
                    auditDetails.put("productId", productId);
                    auditDetails.put("quantityChange", quantity);
                    auditDetails.put("reason", "PRODUCT_NOT_FOUND");
                    auditLogService.logAction("PRODUCT_QUANTITY_UPDATE_FAILED", productId, userEmail, auditDetails);

                    return new ProductNotFoundException(
                            messageSource.getMessage("product.not.found", new Object[]{productId}, locale));
                });

        int oldQuantity = product.getQuantity();
        int newQuantity = oldQuantity - quantity;
        if (newQuantity < 0) {
            logger.error("[SERVICE] Quantity update failed: Insufficient stock for product ID: {}", productId);

            // Log audit for failed quantity update
            String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
            Map<String, Object> auditDetails = new HashMap<>();
            auditDetails.put("productId", productId);
            auditDetails.put("oldQuantity", oldQuantity);
            auditDetails.put("quantityChange", quantity);
            auditDetails.put("reason", "INSUFFICIENT_STOCK");
            auditLogService.logAction("PRODUCT_QUANTITY_UPDATE_FAILED", productId, userEmail, auditDetails);

            throw new IllegalArgumentException(
                    messageSource.getMessage("product.insufficient.stock", new Object[]{productId}, locale));
        }

        product.setQuantity(newQuantity);
        product.setLastUpdated(new Date());
        Product updatedProduct = productRepository.save(product);

        boolean lowStockNotificationSent = false;
        if (updatedProduct.getQuantity() < updatedProduct.getReorderLevel()) {
            notificationService.sendLowStockNotification(
                    "manager@shop.com",
                    updatedProduct.getName(),
                    updatedProduct.getQuantity(),
                    updatedProduct.getReorderLevel()
            );
            lowStockNotificationSent = true;
        }

        logger.info("[SERVICE] Product quantity updated successfully: {}", updatedProduct.getProductId());

        // Log audit for successful quantity update
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        Map<String, Object> auditDetails = new HashMap<>();
        auditDetails.put("name", product.getName());
        auditDetails.put("oldQuantity", oldQuantity);
        auditDetails.put("quantityChange", quantity);
        auditDetails.put("newQuantity", newQuantity);
        auditDetails.put("lowStockNotificationSent", lowStockNotificationSent);
        auditLogService.logAction("PRODUCT_QUANTITY_UPDATED", productId, userEmail, auditDetails);

        return updatedProduct;
    }
}