package com.smartretail.backend.service;

import com.smartretail.backend.models.Product;
import com.smartretail.backend.repository.ProductRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;

    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public List<Product> getAllProducts() {
        System.out.println("[SERVICE] Fetching all products.");
        return productRepository.findAll();
    }

    @Override
    public Product getProductById(String productId) {
        System.out.println("[SERVICE] Fetching product with ID: " + productId);
        return productRepository.findByProductId(productId).orElse(null);
    }

    @Override
    public Product createProduct(Product product) {
        System.out.println("[SERVICE] Creating product: " + product.getName());
        String productId = "p" + UUID.randomUUID().toString().substring(0, 8);
        product.setProductId(productId);
        if (productRepository.existsByProductId(productId)) {
            System.out.println("[SERVICE] Create failed: Product ID already exists: " + productId);
            throw new RuntimeException("Product ID already exists");
        }
        String userId = SecurityContextHolder.getContext().getAuthentication().getName(); // Get logged-in user's email
        product.setAddedBy(userId);
        product.setLastUpdated(new Date());
        Product savedProduct = productRepository.save(product);
        System.out.println("[SERVICE] Product created successfully. ID: " + savedProduct.getProductId());
        return savedProduct;
    }

    @Override
    public Product updateProduct(String productId, Product updateData) {
        System.out.println("[SERVICE] Updating product ID: " + productId);
        Product product = productRepository.findByProductId(productId)
                .orElseThrow(() -> {
                    System.out.println("[SERVICE] Update failed: Product not found for ID: " + productId);
                    return new RuntimeException("Product not found");
                });
        if (updateData.getName() != null) product.setName(updateData.getName());
        if (updateData.getCategory() != null) product.setCategory(updateData.getCategory());
        if (updateData.getPrice() != 0) product.setPrice(updateData.getPrice());
        if (updateData.getQuantity() != 0) product.setQuantity(updateData.getQuantity());
        if (updateData.getMinQuantity() != 0) product.setMinQuantity(updateData.getMinQuantity());
        if (updateData.getExpiryDate() != null) product.setExpiryDate(updateData.getExpiryDate());
        if (updateData.getImageUrl() != null) product.setImageUrl(updateData.getImageUrl());
        product.setLastUpdated(new Date());
        Product updatedProduct = productRepository.save(product);
        System.out.println("[SERVICE] Product updated successfully: " + updatedProduct.getProductId());
        return updatedProduct;
    }

    @Override
    public void deleteProduct(String productId) {
        System.out.println("[SERVICE] Deleting product ID: " + productId);
        if (!productRepository.existsByProductId(productId)) {
            System.out.println("[SERVICE] Delete failed: Product not found for ID: " + productId);
            throw new RuntimeException("Product not found");
        }
        productRepository.deleteByProductId(productId);
        System.out.println("[SERVICE] Product deleted successfully.");
    }
}