package com.smartretail.backend.service;

import com.smartretail.backend.exception.DuplicateResourceException;
import com.smartretail.backend.exception.ResourceNotFoundException;
import com.smartretail.backend.exception.ProductNotFoundException;
import com.smartretail.backend.models.Product;
import com.smartretail.backend.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {
    private static final Logger logger = LoggerFactory.getLogger(ProductServiceImpl.class);
    private final ProductRepository productRepository;
    private final NotificationService notificationService;
    private final FileService fileService;
    private final MessageSource messageSource;

    public ProductServiceImpl(ProductRepository productRepository,
                              NotificationService notificationService,
                              FileService fileService,
                              MessageSource messageSource) {
        this.productRepository = productRepository;
        this.notificationService = notificationService;
        this.fileService = fileService;
        this.messageSource = messageSource;
    }

    // ADD THIS MISSING METHOD
    @Override
    public boolean existsByProductId(String productId) {
        logger.debug("[SERVICE] Checking if product exists: {}", productId);
        return productRepository.existsByProductId(productId);
    }

    @Override
    @Transactional
    public Product addProduct(Product product, Locale locale) {
        logger.debug("[SERVICE] Adding product: {}", product.getName());
        if (productRepository.existsByProductId(product.getProductId())) {
            logger.error("[SERVICE] Add failed: Product ID already exists: {}", product.getProductId());
            throw new IllegalArgumentException(
                    messageSource.getMessage("product.exists", new Object[]{product.getProductId()}, locale));
        }
        product.setLastUpdated(new Date());
        Product savedProduct = productRepository.save(product);
        logger.info("[SERVICE] Product added successfully: {}", savedProduct.getProductId());
        return savedProduct;
    }

    @Override
    public Product getProductById(String productId, Locale locale) {
        logger.debug("[SERVICE] Fetching product with ID: {}", productId);
        return productRepository.findByProductId(productId)
                .orElseThrow(() -> {
                    logger.error("[SERVICE] Product not found for ID: {}", productId);
                    return new ProductNotFoundException(
                            messageSource.getMessage("product.not.found", new Object[]{productId}, locale));
                });
    }

    @Override
    public List<Product> getAllProducts() {
        logger.debug("[SERVICE] Fetching all products.");
        return productRepository.findAll();
    }

    @Override
    public Product createProduct(Product product, MultipartFile imageFile, Locale locale) throws IOException {
        logger.debug("[SERVICE] Creating product: {}", product.getName());
        if (productRepository.existsByProductId(product.getProductId())) {
            logger.error("[SERVICE] Create failed: Product ID already exists: {}", product.getProductId());
            throw new DuplicateResourceException("product.exists", product.getProductId());
        }
        if (imageFile != null && !imageFile.isEmpty()) {
            String imageId = fileService.uploadImage(imageFile, "product_" + product.getProductId());
            product.setImageId(imageId);
        }
        product.setCreatedAt(new Date());
        product.setLastUpdated(new Date());
        Product savedProduct = productRepository.save(product);
        logger.info("[SERVICE] Product created successfully: {}", savedProduct.getProductId());
        return savedProduct;
    }

    @Override
    @Transactional
    public Product updateProduct(String productId, Product product, Locale locale) {
        try {
            return updateProduct(productId, product, null, locale);
        } catch (IOException e) {
            logger.error("[SERVICE] Update failed for product ID {}: {}", productId, e.getMessage());
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
                    return new ResourceNotFoundException("product.not.found", productId);
                });

        if (product.getName() != null) {
            existingProduct.setName(product.getName());
        }
        if (product.getCategory() != null) {
            existingProduct.setCategory(product.getCategory());
        }
        if (product.getPrice() > 0) {
            existingProduct.setPrice(product.getPrice());
        }
        if (product.getQuantity() >= 0) {
            existingProduct.setQuantity(product.getQuantity());
            if (existingProduct.getQuantity() < existingProduct.getReorderLevel()) {
                notificationService.sendLowStockNotification(
                        "manager@shop.com",
                        existingProduct.getName(),
                        existingProduct.getQuantity(),
                        existingProduct.getReorderLevel()
                );
            }
        }
        if (product.getReorderLevel() >= 0) {
            existingProduct.setReorderLevel(product.getReorderLevel());
        }
        if (product.getExpiryDate() != null) {
            existingProduct.setExpiryDate(product.getExpiryDate());
        }
        if (imageFile != null && !imageFile.isEmpty()) {
            String imageId = fileService.uploadImage(imageFile, "product_" + productId);
            existingProduct.setImageId(imageId);
        } else if (product.getImageId() != null) {
            if (product.getImageId().isEmpty()) {
                existingProduct.setImageId(null);
            } else {
                existingProduct.setImageId(product.getImageId());
            }
        }
        existingProduct.setLastUpdated(new Date());
        Product updatedProduct = productRepository.save(existingProduct);
        logger.info("[SERVICE] Product updated successfully: {}", updatedProduct.getProductId());
        return updatedProduct;
    }

    @Override
    @Transactional
    public void deleteProduct(String productId, Locale locale) {
        logger.debug("[SERVICE] Deleting product ID: {}", productId);
        Product product = productRepository.findByProductId(productId)
                .orElseThrow(() -> {
                    logger.error("[SERVICE] Delete failed: Product not found for ID: {}", productId);
                    return new ProductNotFoundException(
                            messageSource.getMessage("product.not.found", new Object[]{productId}, locale));
                });
        productRepository.deleteByProductId(productId);
        logger.info("[SERVICE] Product deleted successfully: {}", productId);
    }

    @Override
    public List<Product> getLowStockProducts() {
        logger.debug("[SERVICE] Fetching low stock products.");
        return productRepository.findLowStockProducts();
    }

    @Override
    public List<Product> getExpiringProducts(Date threshold) {
        logger.debug("[SERVICE] Fetching expiring products before: {}", threshold);
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
            throw new IllegalArgumentException(
                    messageSource.getMessage("product.restock.invalid.quantity", new Object[]{restockQty}, locale));
        }
        Product product = productRepository.findByProductId(productId)
                .orElseThrow(() -> {
                    logger.error("[SERVICE] Restock failed: Product not found for ID: {}", productId);
                    return new ProductNotFoundException(
                            messageSource.getMessage("product.not.found", new Object[]{productId}, locale));
                });
        product.setQuantity(product.getQuantity() + restockQty);
        product.setLastUpdated(new Date());
        Product updatedProduct = productRepository.save(product);
        if (updatedProduct.getQuantity() < updatedProduct.getReorderLevel()) {
            notificationService.sendLowStockNotification(
                    "manager@shop.com",
                    updatedProduct.getName(),
                    updatedProduct.getQuantity(),
                    updatedProduct.getReorderLevel()
            );
        }
        logger.info("[SERVICE] Product restocked successfully: {}", updatedProduct.getProductId());
        return updatedProduct;
    }

    @Override
    @Transactional
    public Product updateProductQuantity(String productId, int quantity, Locale locale) {
        logger.debug("[SERVICE] Updating quantity for product ID: {} by: {}", productId, quantity);
        Product product = productRepository.findByProductId(productId)
                .orElseThrow(() -> {
                    logger.error("[SERVICE] Quantity update failed: Product not found for ID: {}", productId);
                    return new ProductNotFoundException(
                            messageSource.getMessage("product.not.found", new Object[]{productId}, locale));
                });
        int newQuantity = product.getQuantity() - quantity;
        if (newQuantity < 0) {
            logger.error("[SERVICE] Quantity update failed: Insufficient stock for product ID: {}", productId);
            throw new IllegalArgumentException(
                    messageSource.getMessage("product.insufficient.stock", new Object[]{productId}, locale));
        }
        product.setQuantity(newQuantity);
        product.setLastUpdated(new Date());
        Product updatedProduct = productRepository.save(product);
        if (updatedProduct.getQuantity() < updatedProduct.getReorderLevel()) {
            notificationService.sendLowStockNotification(
                    "manager@shop.com",
                    updatedProduct.getName(),
                    updatedProduct.getQuantity(),
                    updatedProduct.getReorderLevel()
            );
        }
        logger.info("[SERVICE] Product quantity updated successfully: {}", updatedProduct.getProductId());
        return updatedProduct;
    }
}