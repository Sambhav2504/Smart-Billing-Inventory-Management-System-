package com.smartretail.backend.service;

import com.smartretail.backend.models.Product;
import com.smartretail.backend.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final NotificationService notificationService;

    public ProductServiceImpl(ProductRepository productRepository, NotificationService notificationService) {
        this.productRepository = productRepository;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional
    public Product addProduct(Product product) {
        System.out.println("[SERVICE] Adding product: " + product.getName());
        if (productRepository.existsByProductId(product.getProductId())) {
            System.out.println("[SERVICE] Add failed: Product ID already exists: " + product.getProductId());
            throw new RuntimeException("Product ID already exists");
        }
        product.setLastUpdated(new Date());
        Product savedProduct = productRepository.save(product);
        System.out.println("[SERVICE] Product added successfully: " + savedProduct.getProductId());
        return savedProduct;
    }

    @Override
    public Product getProductById(String productId) {
        System.out.println("[SERVICE] Fetching product with ID: " + productId);
        return productRepository.findByProductId(productId)
                .orElseThrow(() -> {
                    System.out.println("[SERVICE] Product not found for ID: " + productId);
                    return new RuntimeException("Product not found");
                });
    }

    @Override
    public List<Product> getAllProducts() {
        System.out.println("[SERVICE] Fetching all products.");
        return productRepository.findAll();
    }

    @Override
    @Transactional
    public Product updateProduct(String productId, Product updateData) {
        System.out.println("[SERVICE] Updating product ID: " + productId);
        Product product = productRepository.findByProductId(productId)
                .orElseThrow(() -> {
                    System.out.println("[SERVICE] Update failed: Product not found for ID: " + productId);
                    return new RuntimeException("Product not found");
                });
        if (updateData.getName() != null) product.setName(updateData.getName());
        if (updateData.getCategory() != null) product.setCategory(updateData.getCategory());
        if (updateData.getPrice() > 0) product.setPrice(updateData.getPrice());
        if (updateData.getQuantity() >= 0) {
            product.setQuantity(updateData.getQuantity());
            // Check low stock after update
            if (product.getQuantity() < product.getReorderLevel()) {
                notificationService.sendLowStockNotification(
                        "manager@shop.com",
                        product.getName(),
                        product.getQuantity(),
                        product.getReorderLevel()
                );
            }
        }
        if (updateData.getMinQuantity() >= 0) product.setMinQuantity(updateData.getMinQuantity());
        if (updateData.getReorderLevel() >= 0) product.setReorderLevel(updateData.getReorderLevel());
        if (updateData.getExpiryDate() != null) product.setExpiryDate(updateData.getExpiryDate());
        product.setLastUpdated(new Date());
        Product updatedProduct = productRepository.save(product);
        System.out.println("[SERVICE] Product updated successfully: " + updatedProduct.getProductId());
        return updatedProduct;
    }

    @Override
    @Transactional
    public void deleteProduct(String productId) {
        System.out.println("[SERVICE] Deleting product ID: " + productId);
        Product product = productRepository.findByProductId(productId)
                .orElseThrow(() -> {
                    System.out.println("[SERVICE] Delete failed: Product not found for ID: " + productId);
                    return new RuntimeException("Product not found");
                });
        productRepository.deleteById(product.getProductId());
        System.out.println("[SERVICE] Product deleted successfully.");
    }

    @Override
    public List<Product> getLowStockProducts() {
        return productRepository.findLowStockProducts();
    }

    @Override
    @Transactional
    public Product restockProduct(String productId, int restockQty) {
        System.out.println("[SERVICE] Restocking product ID: " + productId + " with quantity: " + restockQty);
        Product product = productRepository.findByProductId(productId)
                .orElseThrow(() -> {
                    System.out.println("[SERVICE] Restock failed: Product not found for ID: " + productId);
                    return new RuntimeException("Product not found");
                });
        product.setQuantity(product.getQuantity() + restockQty);
        product.setLastUpdated(new Date());
        Product updatedProduct = productRepository.save(product);
        // Check low stock after restock
        if (updatedProduct.getQuantity() < updatedProduct.getReorderLevel()) {
            notificationService.sendLowStockNotification(
                    "manager@shop.com",
                    updatedProduct.getName(),
                    updatedProduct.getQuantity(),
                    updatedProduct.getReorderLevel()
            );
        }
        System.out.println("[SERVICE] Product restocked successfully: " + updatedProduct.getProductId());
        return updatedProduct;
    }

    @Override
    public List<Product> getExpiringProducts(Date threshold) {
        return productRepository.findByExpiryDateBefore(threshold).stream()
                .filter(product -> product.getExpiryDate() != null)
                .collect(Collectors.toList());
    }
}